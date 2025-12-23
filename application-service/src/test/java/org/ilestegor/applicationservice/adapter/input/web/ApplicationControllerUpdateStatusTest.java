package org.ilestegor.applicationservice.adapter.input.web;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.config.AbstractIntegrationTest;
import org.ilestegor.applicationservice.adapter.input.web.config.TestPortsConfig;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationStatusUpdateRequestDto;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.security.interfaces.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Import(TestPortsConfig.class)
class ApplicationControllerUpdateStatusTest extends AbstractIntegrationTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    JwtService jwtService;

    // МОКИ из TestPortsConfig
    @Autowired
    UserPort userPort;
    @Autowired
    VacancyPort vacancyPort;
    @Autowired
    CompanyPort companyPort;
    @Autowired
    ApplicationEventPublisherPort applicationEventPublisherPort;

    // РЕАЛЬНЫЕ репозитории (через БД)
    @Autowired
    ApplicationRepositoryPort applicationRepositoryPort;
    @Autowired
    ApplicationStatusRepositoryPort applicationStatusRepositoryPort;

    private UUID userId;
    private String tokenManager; // роль проходит PreAuthorize
    private String tokenUser;    // роль НЕ проходит PreAuthorize

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tokenManager = generateJwt(userId, "test@mail.com", "ROLE_MANAGER");
        tokenUser = generateJwt(userId, "test@mail.com", "ROLE_USER");

        // дефолты, чтобы никогда не было NPE из-за unstubbed Mono
        when(applicationEventPublisherPort.publishStatusChanged(any())).thenReturn(Mono.empty());

        when(userPort.checkUserExists(any(), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(userId, "FN", "mail")));

        when(vacancyPort.checkVacancyExists(any(), anyString())).thenReturn(Mono.just(true));
        when(vacancyPort.getCompanyIdByVacancyId(any(), anyString())).thenReturn(Mono.just(UUID.randomUUID()));
        when(companyPort.isUserBelongsToCompany(any(), any(), anyString())).thenReturn(Mono.just(true));
        when(vacancyPort.getVacancyTitle(any(), anyString())).thenReturn(Mono.just("Vacancy Title"));
    }

    private String generateJwt(UUID userId, String email, String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        return jwtService.generateAccessToken(auth, userId, List.of(roles));
    }

    private WebTestClient.ResponseSpec patchStatus(UUID appId, String bearerToken, ApplicationStatusName newStatus) {
        return webTestClient.patch()
                .uri("/api/application/{id}/status", appId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ApplicationStatusUpdateRequestDto(newStatus))
                .exchange();
    }


    private ApplicationStatus findStatusByNameOrNull(ApplicationStatusName name) {
        return applicationStatusRepositoryPort.findByName(name).block();
    }

    private Application seedApplication(UUID vacancyId, UUID userId, Long statusId, String coverLetter) {
        var now = LocalDateTime.now().minusMinutes(5);
        var app = Application.builder()
                .vacancyId(vacancyId)
                .userId(userId)
                .status(statusId)
                .coverLetter(coverLetter)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return applicationRepositoryPort.save(app).block();
    }

    // ----------------------- TESTS -----------------------

    @Test
    void updateStatus_forbiddenForRoleUser_shouldReturn403() {
        UUID appId = UUID.randomUUID();

        patchStatus(appId, tokenUser, ApplicationStatusName.REJECTED)
                .expectStatus().isForbidden();
    }

    @Test
    void updateStatus_success_asManager_shouldReturn200_updateDb_andPublishEvent() {
        UUID vacancyId = UUID.randomUUID();

        // старый статус: берём любой существующий id
        Long oldStatusId = 1L;
        assertNotNull(oldStatusId);

        // новый статус: лучше взять тот, который ТОЧНО есть в БД.
        // если "REJECTED" не засеян — тест упадёт. Поэтому подстрахуемся:
        ApplicationStatus newStatus = Stream.of(
                        ApplicationStatusName.REJECTED,
                        ApplicationStatusName.NEW
                )
                .map(this::findStatusByNameOrNull)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Liquibase должен создать хотя бы один status с именем из enum"));

        var saved = seedApplication(vacancyId, userId, oldStatusId, "old");
        assertNotNull(saved);
        UUID appId = saved.getId();

        patchStatus(appId, tokenManager, newStatus.getApplicationStatusName())
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(newStatus.getApplicationStatusName().getValue())
                .jsonPath("$.update_at").exists();

        var fromDb = applicationRepositoryPort.findById(appId).block();
        assertNotNull(fromDb);
        assertEquals(newStatus.getId(), fromDb.getStatus(), "status_id должен обновиться в БД");

        verify(applicationEventPublisherPort, times(1)).publishStatusChanged(any());
        verify(vacancyPort).getVacancyTitle(eq(vacancyId), anyString());
    }

    @Test
    void updateStatus_applicationNotFound_shouldReturn400() {
        UUID missingAppId = UUID.randomUUID();

        patchStatus(missingAppId, tokenManager, ApplicationStatusName.REJECTED)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.title").exists();
    }


    @Test
    void updateStatus_userDoesNotBelongToCompany_shouldReturn404() {
        UUID vacancyId = UUID.randomUUID();
        Long statusId = 1L;
        var app = seedApplication(vacancyId, userId, statusId, "old");

        when(vacancyPort.checkVacancyExists(eq(vacancyId), anyString())).thenReturn(Mono.just(true));
        when(vacancyPort.getCompanyIdByVacancyId(eq(vacancyId), anyString())).thenReturn(Mono.just(UUID.randomUUID()));
        when(companyPort.isUserBelongsToCompany(any(), eq(userId), anyString())).thenReturn(Mono.just(false));

        patchStatus(app.getId(), tokenManager, ApplicationStatusName.REJECTED)
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }
}
