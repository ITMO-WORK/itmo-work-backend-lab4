package org.ilestegor.applicationservice.adapter.input.web.config;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@Tag("integration")
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",

        "spring.kafka.admin.auto-create=false",
        "spring.kafka.admin.fail-fast=false",
        "spring.kafka.bootstrap-servers=localhost:9092",

        "spring.r2dbc.pool.enabled=true",
        "spring.r2dbc.pool.max-size=20",
        "spring.r2dbc.pool.initial-size=0",
        "spring.r2dbc.pool.validation-query=SELECT 1",

        "jwt.secret=VGhpcy1pcy1hLWxvbmcgc2VjcmV0IGtleSBmb3IgdGVzdHM=",
        "jwt.access-ttl=PT15M"
})
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/" + POSTGRES.getDatabaseName());
        r.add("spring.r2dbc.username", POSTGRES::getUsername);
        r.add("spring.r2dbc.password", POSTGRES::getPassword);

        r.add("spring.liquibase.url", POSTGRES::getJdbcUrl);
        r.add("spring.liquibase.user", POSTGRES::getUsername);
        r.add("spring.liquibase.password", POSTGRES::getPassword);
    }
}
