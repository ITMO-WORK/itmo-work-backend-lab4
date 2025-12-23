package org.ilestegor.applicationservice.adapter.input.web;

import org.apache.kafka.common.errors.TimeoutException;
import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.config.AbstractIntegrationTest;
import org.ilestegor.applicationservice.adapter.input.web.config.TestPortsConfig;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(TestPortsConfig.class)
class ApplicationControllerGetAllApplicationsByVacancyIdTest extends AbstractIntegrationTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    JwtService jwtService;

    @Autowired
    ApplicationRepositoryPort applicationRepositoryPort;
    @Autowired
    ApplicationStatusRepositoryPort applicationStatusRepositoryPort;

    @Autowired
    UserPort userPort;
    @Autowired
    VacancyPort vacancyPort;
    @Autowired
    CompanyPort companyPort;

    private UUID currentUserId;
    private UUID vacancyId;
    private UUID companyId;

    private String tokenManager;
    private String tokenUser;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        vacancyId = UUID.randomUUID();
        companyId = UUID.randomUUID();

        tokenManager = generateJwt(currentUserId, "manager@mail.com", "ROLE_MANAGER");
        tokenUser = generateJwt(currentUserId, "user@mail.com", "ROLE_USER");


        when(userPort.checkUserExists(eq(currentUserId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(currentUserId, "Current User", "mail")));

        when(vacancyPort.checkVacancyExists(eq(vacancyId), anyString()))
                .thenReturn(Mono.just(true));

        when(vacancyPort.getCompanyIdByVacancyId(eq(vacancyId), anyString()))
                .thenReturn(Mono.just(companyId));

        when(companyPort.isUserBelongsToCompany(eq(companyId), eq(currentUserId), anyString()))
                .thenReturn(Mono.just(true));

        when(vacancyPort.getVacancyTitle(eq(vacancyId), anyString()))
                .thenReturn(Mono.just("Vacancy Title"));
    }

    private String generateJwt(UUID userId, String email, String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        return jwtService.generateAccessToken(auth, userId, List.of(roles));
    }

    private WebTestClient.ResponseSpec getAll(UUID vacancyId, String token, int page, int size) {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/application")
                        .queryParam("vacancyId", vacancyId)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }


    private Application seedApplication(UUID vacancyId, UUID applicantUserId, Long statusId, String coverLetter) {
        var now = LocalDateTime.now();

        var app = Application.builder()
                .vacancyId(vacancyId)
                .userId(applicantUserId)
                .status(statusId)
                .coverLetter(coverLetter)
                .createdAt(now.minusMinutes(1))
                .updatedAt(now.minusMinutes(1))
                .build();

        return applicationRepositoryPort.save(app).block();
    }

    @Test
    void getAllApplicationsByVacancyId_success_asManager_shouldReturn200_andEnrichedPage() {
        Long statusId = 1L;


        UUID applicantId = UUID.randomUUID();
        seedApplication(vacancyId, applicantId, statusId, "hello");

        when(userPort.checkUserExists(eq(applicantId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(applicantId, "Applicant FN", "mail")));

        getAll(vacancyId, tokenManager, 0, 10)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1);
    }

    @Test
    void getAllApplicationsByVacancyId_forbidden_forRoleUser_shouldReturn403() {
        getAll(vacancyId, tokenUser, 0, 10)
                .expectStatus().isForbidden();
    }

    @Test
    void getAllApplicationsByVacancyId_timeout_shouldReturn200_withEmptyPage() {
        when(vacancyPort.getVacancyTitle(eq(vacancyId), anyString()))
                .thenReturn(Mono.error(new TimeoutException("timeout")));

        getAll(vacancyId, tokenManager, 0, 10)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0);
    }
}
