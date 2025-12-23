package com.itmowork.user_service.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.enable-auto-commit=false"
})
class AuthControllerTest {

    private static final String TEST_JWT_SECRET =
            "72eadf75ac2a262555bdda7b35a69c9c5b42f4f4d4299efc3ca272087892f1d5";

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
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
        r.add("spring.liquibase.enabled", () -> true);
        r.add("jwt.secret", () -> TEST_JWT_SECRET);
    }

    @Autowired WebTestClient webTestClient;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper objectMapper;
    @Autowired PasswordEncoder passwordEncoder;
    @MockitoBean KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setup() {
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        upsertRole("ROLE_USER");
        upsertRole("ROLE_COMPANY_OWNER");
        upsertRole("ROLE_ADMIN");
    }

    @AfterEach
    void tearDown() {
        jdbc.execute("TRUNCATE TABLE user_roles, users RESTART IDENTITY CASCADE");
    }

    @Test
    @DisplayName("POST /api/auth/register с ADMIN JWT -> 200 OK, user created, ROLE_USER, token returned")
    void register_shouldCreateUser_andReturnToken_whenAdmin() throws Exception {
        String adminEmail = "admin@example.com";
        UUID adminId = seedAdmin(adminEmail, "Admin", "does-not-matter");
        String adminJwt = generateJwt(adminId, adminEmail, "ROLE_ADMIN");

        String email = "john@example.com";
        Map<String, Object> body = Map.of(
                "full_name", "John",
                "email", email,
                "password", "password123"
        );

        byte[] bytes = webTestClient.post()
                .uri("/api/auth/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .returnResult()
                .getResponseBody();

        assertThat(bytes).isNotNull();
        UUID createdId = extractId(new String(bytes));

        String fullname = jdbc.queryForObject(
                "select fullname from users where id = ?",
                String.class,
                createdId
        );
        assertThat(fullname).isEqualTo("John");

        String emailDb = jdbc.queryForObject(
                "select email from users where id = ?",
                String.class,
                createdId
        );
        assertThat(emailDb).isEqualTo(email);

        List<String> roles = jdbc.queryForList("""
                select r.name
                from roles r
                join user_roles ur on ur.role_id = r.id
                where ur.user_id = ?
                """, String.class, createdId);

        assertThat(roles).contains("ROLE_USER");
    }

    @Test
    @DisplayName("POST /api/auth/register без JWT -> 401 UNAUTHORIZED")
    void register_withoutJwt_shouldReturn401() {
        Map<String, Object> body = Map.of(
                "full_name", "John",
                "email", "john@example.com",
                "password", "password123"
        );

        webTestClient.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /api/auth/register -> 4xx when user already exists (with ADMIN JWT)")
    void register_shouldReturn4xx_whenUserAlreadyExists_withAdmin() {
        String adminEmail = "admin@example.com";
        UUID adminId = seedAdmin(adminEmail, "Admin", "does-not-matter");
        String adminJwt = generateJwt(adminId, adminEmail, "ROLE_ADMIN");

        String email = "dup@example.com";
        seedUserWithRole(email, "Existing", "hash", "ROLE_USER");

        Map<String, Object> body = Map.of(
                "full_name", "Another",
                "email", email,
                "password", "password123"
        );

        webTestClient.post()
                .uri("/api/auth/register")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().is4xxClientError();

        Integer cnt = jdbc.queryForObject("select count(*) from users where email = ?", Integer.class, email);
        assertThat(cnt).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/auth/register-company-owner с ADMIN JWT -> 200 OK, ROLE_COMPANY_OWNER")
    void registerCompanyOwner_shouldCreateCompanyOwner_andReturnToken() throws Exception {
        String adminEmail = "admin@example.com";
        UUID adminId = seedAdmin(adminEmail, "Admin", "does-not-matter");
        String adminJwt = generateJwt(adminId, adminEmail, "ROLE_ADMIN");

        Map<String, Object> body = Map.of(
                "full_name", "Boss",
                "email", "owner@example.com",
                "password", "ownerPass"
        );

        byte[] bytes = webTestClient.post()
                .uri("/api/auth/register-company-owner")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .returnResult()
                .getResponseBody();

        assertThat(bytes).isNotNull();
        UUID createdId = extractId(new String(bytes));

        List<String> roles = jdbc.queryForList("""
                select r.name
                from roles r
                join user_roles ur on ur.role_id = r.id
                where ur.user_id = ?
                """, String.class, createdId);

        assertThat(roles).contains("ROLE_COMPANY_OWNER");
    }

    @Test
    @DisplayName("POST /api/auth/login -> 200 OK, returns token when credentials are valid")
    void login_shouldReturnToken_whenCredentialsValid() {
        String email = "john@example.com";
        String password = "password123";

        UUID userId = seedUser(email, "John", password, "ROLE_USER");

        Map<String, Object> body = Map.of(
                "email", email,
                "password", password
        );

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").value(v -> assertThat(v.toString()).isEqualTo(userId.toString()))
                .jsonPath("$.token").isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/auth/login -> 4xx when password is invalid")
    void login_shouldReturn4xx_whenPasswordInvalid() {
        String email = "john@example.com";
        seedUser(email, "John", "correctPassword", "ROLE_USER");

        Map<String, Object> body = Map.of(
                "email", email,
                "password", "wrongPassword"
        );

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                // чаще всего будет 401, но если у тебя маппинг исключений другой — всё равно 4xx
                .expectStatus().is4xxClientError();
    }



    private String generateJwt(UUID userId, String email, String... roles) {
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(TEST_JWT_SECRET));

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("roles", List.of(roles))
                .signWith(key)
                .compact();
    }

    private UUID extractId(String json) throws Exception {
        JsonNode node = objectMapper.readTree(json);
        JsonNode idNode = node.get("user_id");
        if (idNode == null) idNode = node.get("id");
        assertThat(idNode).isNotNull();
        return UUID.fromString(idNode.asText());
    }

    private void upsertRole(String name) {
        Integer exists = jdbc.queryForObject("select count(*) from roles where name = ?", Integer.class, name);
        if (exists != null && exists > 0) return;

        jdbc.update("insert into roles(name) values (?)", name);
    }

    private UUID seedUserWithRole(String email, String fullName, String passwordHash, String roleName) {
        upsertRole(roleName);

        UUID id = jdbc.queryForObject("""
                insert into users(fullname, email, password)
                values (?, ?, ?)
                returning id
                """, UUID.class, fullName, email, passwordHash);

        Long roleId = jdbc.queryForObject("select id from roles where name = ?", Long.class, roleName);
        jdbc.update("insert into user_roles(user_id, role_id) values (?, ?)", id, roleId);

        return id;
    }

    private UUID seedAdmin(String email, String fullName, String passwordHashOrEncoded) {
        return seedUserWithRole(email, fullName, passwordHashOrEncoded, "ROLE_ADMIN");
    }

    private UUID seedUser(String email, String fullName, String rawPassword, String roleName) {
        upsertRole(roleName);

        String encoded = passwordEncoder.encode(rawPassword);

        UUID id = jdbc.queryForObject("""
            insert into users(fullname, email, password)
            values (?, ?, ?)
            returning id
            """, UUID.class, fullName, email, encoded);

        Long roleId = jdbc.queryForObject("select id from roles where name = ?", Long.class, roleName);
        jdbc.update("insert into user_roles(user_id, role_id) values (?, ?)", id, roleId);

        return id;
    }



}
