package com.itmowork.user_service.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpHeaders;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.jackson.property-naming-strategy=SNAKE_CASE",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.admin.fail-fast=false",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class UserControllerTest {

    private static final String TEST_JWT_SECRET =
            "72eadf75ac2a262555bdda7b35a69c9c5b42f4f4d4299efc3ca272087892f1d5";

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("user-db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        // включи то, чем реально мигрируешь
        r.add("spring.liquibase.enabled", () -> true);
        // если Flyway:
        // r.add("spring.flyway.enabled", () -> true);
    }

    @Autowired WebTestClient webTestClient;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;

    // Kafka stub (если у тебя контекст требует KafkaTemplate)
    @MockitoBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setup() {
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @AfterEach
    void tearDown() {
        // чистим то, что могло создаться
        // роли не обязательны для GET, но если у тебя FK/constraints — можешь оставить как в AuthControllerIT
        jdbc.execute("TRUNCATE TABLE user_roles, users RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("GET /api/user/{id} with ADMIN JWT -> 200 OK, returns user data when exists")
    void findUserById_shouldReturn200_whenUserExists() {
        UUID userId = seedUser("John", "john@example.com", "hash");

        UUID adminId = UUID.randomUUID();
        String adminJwt = generateJwt(adminId, "admin@example.com", "ROLE_ADMIN");

        webTestClient.get()
                .uri("/api/user/{id}", userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId.toString())
                .jsonPath("$.full_name").isEqualTo("John")
                .jsonPath("$.email").isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("GET /api/user/{id} without JWT -> 401 UNAUTHORIZED")
    void findUserById_withoutJwt_shouldReturn401() {
        UUID userId = seedUser("John", "john@example.com", "hash");

        webTestClient.get()
                .uri("/api/user/{id}", userId)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /api/user/{id} -> 4xx when user not found")
    void findUserById_shouldReturn4xx_whenUserNotFound() {
        UUID missingId = UUID.randomUUID();

        webTestClient.get()
                .uri("/api/user/{id}", missingId)
                .exchange()
                .expectStatus().is4xxClientError();
    }

    // ---------------- HELPERS ----------------

    private UUID seedUser(String fullName, String email, String passwordHash) {
        UUID id = jdbc.queryForObject("""
                insert into users(fullname, email, password)
                values (?, ?, ?)
                returning id
                """, UUID.class, fullName, email, passwordHash);

        assertThat(id).isNotNull();
        return id;
    }

    private String generateJwt(UUID userId, String email, String... roles) {
        javax.crypto.SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_JWT_SECRET));

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("roles", List.of(roles))
                .signWith(key)
                .compact();
    }
}
