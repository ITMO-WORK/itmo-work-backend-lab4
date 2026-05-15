package org.ilestegor.applicationservice.adapter.input.web;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.config.AbstractIntegrationTest;
import org.ilestegor.applicationservice.adapter.input.web.config.TestPortsConfig;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.security.interfaces.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Import(TestPortsConfig.class)
@Tag("integration")
class ApplicationControllerUpdateTest extends AbstractIntegrationTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    JwtService jwtService;


    @Autowired
    ApplicationRepositoryPort applicationRepositoryPort;
    @Autowired
    DatabaseClient databaseClient;


    @Autowired
    UserPort userPort;
    @Autowired
    VacancyPort vacancyPort;

    private UUID userId;
    private String token;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        token = generateJwt(userId, "test@mail.com", "ROLE_USER");
    }

    @AfterEach
    void cleanup() {
        databaseClient.sql("DELETE FROM applications").fetch().rowsUpdated().block();
    }


    private String generateJwt(UUID userId, String email, String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        return jwtService.generateAccessToken(auth, userId, List.of(roles));
    }

    private WebTestClient.ResponseSpec patchUpdate(UUID applicationId, String coverLetter) {
        return webTestClient.patch()
                .uri("/api/application/{id}", applicationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApplicationCreateRequestDto(coverLetter))
                .exchange();
    }

    private Application seedApplication(UUID vacancyId, UUID ownerId, long statusId, String coverLetter) {
        var now = LocalDateTime.now().minusMinutes(10);

        var app = Application.builder()
                .userId(ownerId)
                .vacancyId(vacancyId)
                .status(statusId)
                .coverLetter(coverLetter)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return applicationRepositoryPort.save(app).block();
    }

    private Long anyExistingStatusId() {
        return databaseClient.sql("SELECT id FROM application_status LIMIT 1")
                .map((row, meta) -> row.get("id", Long.class))
                .one()
                .block();
    }


    @Test
    void updateApplication_success_shouldReturn200_andUpdateDb() {
        UUID vacancyId = UUID.randomUUID();


        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(userId, "FN", "mail")));


        when(vacancyPort.checkVacancyExists(eq(vacancyId), anyString())).thenReturn(Mono.just(true));
        when(vacancyPort.checkVacancyIsPublished(eq(vacancyId), anyString())).thenReturn(Mono.just(true));

        Long statusId = anyExistingStatusId();

        var appId = seedApplication(vacancyId, userId, statusId, "old");

        patchUpdate(appId.getId(), "new cover")
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(appId.getId().toString())
                .jsonPath("$.cover_letter").isEqualTo("new cover")
                .jsonPath("$.updated_at").exists();


        var saved = applicationRepositoryPort.findById(appId.getId()).block();
        Assertions.assertNotNull(saved);
        Assertions.assertEquals("new cover", saved.getCoverLetter());
    }

    @Test
    void updateApplication_userNotFound_shouldReturn404() {
        UUID vacancyId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();


        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(null, "x", "x")));


        patchUpdate(applicationId, "x")
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void updateApplication_applicationNotFound_shouldReturn400() {
        UUID applicationId = UUID.randomUUID();

        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(userId, "FN", "mail")));

        patchUpdate(applicationId, "x")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void updateApplication_vacancyNotFound_shouldReturn404() {
        UUID vacancyId = UUID.randomUUID();

        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(userId, "FN", "mail")));

        when(vacancyPort.checkVacancyExists(eq(vacancyId), argThat(t -> t.startsWith("Bearer "))))
                .thenReturn(Mono.just(false));

        when(vacancyPort.checkVacancyIsPublished(any(), anyString()))
                .thenReturn(Mono.just(true));

        Long statusId = anyExistingStatusId();
        var app = seedApplication(vacancyId, userId, statusId, "old");

        patchUpdate(app.getId(), "x")
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void updateApplication_vacancyNotPublished_shouldReturn400() {
        UUID vacancyId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(userId, "FN", "mail")));

        when(vacancyPort.checkVacancyExists(eq(vacancyId), anyString()))
                .thenReturn(Mono.just(true));
        when(vacancyPort.checkVacancyIsPublished(eq(vacancyId), anyString()))
                .thenReturn(Mono.just(false));

        Long statusId = anyExistingStatusId();
        seedApplication(vacancyId, userId, statusId, "old");

        patchUpdate(applicationId, "x")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }
}