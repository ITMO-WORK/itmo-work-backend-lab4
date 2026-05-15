package com.itmowork.company_service.controller;

import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.configuration.UserPrincipal;
import com.itmowork.company_service.security.JwtService;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",

        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "jwt.secret=VGhpcy1pcy1hLWxvbmcgc2VjcmV0IGtleSBmb3IgdGVzdHM=",
        "jwt.access-ttl=PT15M"

})
public abstract class AbstractIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" +
                        postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());

        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> true);
    }

    @Autowired
    protected WebTestClient baseClient;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected CompanyRepositoryPort companyRepository;

    protected WebTestClient client;

    protected WebTestClient withJwt(UUID userId, String email, String... roles) {
        String jwt = generateJwt(userId, email, roles);

        return baseClient.mutate()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .filter(addAuthToken(userId, email))
                .build();
    }

    protected String generateJwt(UUID userId, String email, String... roles) {
        return jwtService.generateAccessToken(
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        Arrays.stream(roles)
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                ),
                userId,
                List.of(roles)
        );
    }

    protected ExchangeFilterFunction addAuthToken(UUID userId, String email) {
        return (request, next) ->
                next.exchange(request)
                        .contextWrite(
                                ReactiveSecurityContextHolder.withAuthentication(
                                        new UsernamePasswordAuthenticationToken(
                                                new UserPrincipal(email, userId),
                                                null,
                                                List.of()
                                        )
                                )
                        );
    }
}

