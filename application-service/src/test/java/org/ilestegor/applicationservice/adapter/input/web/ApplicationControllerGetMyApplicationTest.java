package org.ilestegor.applicationservice.adapter.input.web;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.config.AbstractIntegrationTest;
import org.ilestegor.applicationservice.adapter.input.web.config.TestPortsConfig;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.ApplicationStatusRepositoryPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.security.interfaces.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(TestPortsConfig.class)
@Tag("integration")
class ApplicationControllerGetMyApplicationTest extends AbstractIntegrationTest {

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

    private UUID currentUserId;
    private String token;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        token = generateJwt(currentUserId, "me@mail.com", "ROLE_USER");


        when(userPort.checkUserExists(eq(currentUserId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(currentUserId, "Me FullName", "mail")));
    }

    private String generateJwt(UUID userId, String email, String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        return jwtService.generateAccessToken(auth, userId, List.of(roles));
    }

    private WebTestClient.ResponseSpec getMe(int page, int size) {
        return webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/application/me")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }


    private String anyExistingStatusValue(Long id) {
        return applicationStatusRepositoryPort.findById(id)
                .map(s -> s.getApplicationStatusName().getValue())
                .block();
    }

    private Application seedApplication(UUID userId, UUID vacancyId, Long statusId, String cover) {
        var now = LocalDateTime.now();

        var app = Application.builder()
                .userId(userId)
                .vacancyId(vacancyId)
                .status(statusId)
                .coverLetter(cover)
                .createdAt(now.minusMinutes(1))
                .updatedAt(now.minusMinutes(1))
                .build();

        return applicationRepositoryPort.save(app).block();
    }

    @Test
    void getMyApplications_success_shouldReturn200_andEnrichedContent() {
        Long statusId = 1L;

        String statusValue = anyExistingStatusValue(statusId);
        if (statusValue == null) throw new AssertionError("Не удалось прочитать статус из БД");

        UUID vacancyId = UUID.randomUUID();
        seedApplication(currentUserId, vacancyId, statusId, "cover");


        when(vacancyPort.getVacancyTitle(eq(vacancyId), anyString()))
                .thenReturn(Mono.just("Vacancy #1"));

        when(userPort.checkUserExists(eq(currentUserId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(currentUserId, "Me FullName", "mail")));

        getMe(0, 10)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1);
    }

    @Test
    void getMyApplications_timeoutInEnrich_shouldReturn200_andFallbackContentWithoutEnrichment() {
        Long statusId = 1L;


        UUID vacancyId = UUID.randomUUID();
        seedApplication(currentUserId, vacancyId, statusId, "cover");


        when(vacancyPort.getVacancyTitle(eq(vacancyId), anyString()))
                .thenReturn(Mono.error(new TimeoutException("timeout")));

        getMe(0, 10)
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)


                .jsonPath("$.content[0].vacancyTitle").doesNotExist()
                .jsonPath("$.content[0].userFullName").doesNotExist()
                .jsonPath("$.content[0].applicationStatus").doesNotExist();
    }

    @Test
    void getMyApplications_userNotFound_shouldReturn404() {

        when(userPort.checkUserExists(eq(currentUserId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(null, "x", "x")));

        getMe(0, 10)
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.valueOf("application/problem+json"))
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.title").isEqualTo("User not found");
    }
}
