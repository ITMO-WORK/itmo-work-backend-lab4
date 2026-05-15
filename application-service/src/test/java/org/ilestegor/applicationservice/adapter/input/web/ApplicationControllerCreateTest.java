package org.ilestegor.applicationservice.adapter.input.web;

import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.input.web.config.AbstractIntegrationTest;
import org.ilestegor.applicationservice.adapter.input.web.config.TestPortsConfig;
import org.ilestegor.applicationservice.adapter.input.web.dto.request.ApplicationCreateRequestDto;
import org.ilestegor.applicationservice.application.port.output.*;
import org.ilestegor.applicationservice.domain.Application;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.ilestegor.applicationservice.security.interfaces.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@Import(TestPortsConfig.class)
@AutoConfigureWebTestClient
@Tag("integration")
class ApplicationControllerCreateTest extends AbstractIntegrationTest {

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
    ApplicationEventPublisherPort applicationEventPublisherPort;

    private UUID userId;
    private String token;

    @BeforeEach
    void setup() {
        reset(userPort, vacancyPort, applicationEventPublisherPort);

        userId = UUID.randomUUID();
        token = generateJwt(userId, "test@mail.com", "ROLE_USER");

        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(userId, "John Doe", "test@mail.com")));

        when(vacancyPort.checkVacancyExists(any(UUID.class), anyString()))
                .thenReturn(Mono.just(true));
        when(vacancyPort.checkVacancyIsPublished(any(UUID.class), anyString()))
                .thenReturn(Mono.just(true));

        when(applicationEventPublisherPort.publishApplicationCreate(any()))
                .thenReturn(Mono.empty());
        applicationRepositoryPort.deleteAll().block();

    }

    private String generateJwt(UUID userId, String email, String... roles) {
        var authorities = Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList();

        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);

        return jwtService.generateAccessToken(auth, userId, List.of(roles));
    }

    private ApplicationStatus status(ApplicationStatusName name) {
        var st = new ApplicationStatus();
        st.setApplicationStatusName(name);
        return st;
    }

    private WebTestClient.ResponseSpec postCreate(UUID vacancyId, String coverLetter) {
        var req = new ApplicationCreateRequestDto(coverLetter);

        return webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/application")
                        .queryParam("vacancyId", vacancyId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange();
    }

    @Test
    void createApplication_shouldReturn201_andPersistToDb() {
        UUID vacancyId = UUID.randomUUID();
        postCreate(vacancyId, "Hello")
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("new")
                .jsonPath("$.cover_letter").isEqualTo("Hello")
                .jsonPath("$.id").exists();


        Boolean exists = applicationRepositoryPort.existsByUserIdAndVacancyId(userId, vacancyId).block();
        assertEquals(Boolean.TRUE, exists);

        verify(applicationEventPublisherPort).publishApplicationCreate(any());
        verify(vacancyPort).checkVacancyExists(eq(vacancyId), anyString());
        verify(vacancyPort).checkVacancyIsPublished(eq(vacancyId), anyString());
        verify(userPort).checkUserExists(eq(userId), anyString());
    }

    @Test
    void createApplication_userNotFound_shouldReturn404_andNotTouchDbOrKafka() {
        UUID vacancyId = UUID.randomUUID();

        when(userPort.checkUserExists(eq(userId), anyString()))
                .thenReturn(Mono.just(new UserResponseDto(null, "x", "x")));

        postCreate(vacancyId, "Hi")
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);


        StepVerifier.create(applicationRepositoryPort.countApplicationsByUserId(userId))
                .expectNext(0L)
                .verifyComplete();

        verify(applicationEventPublisherPort, never()).publishApplicationCreate(any());
    }

    @Test
    void createApplication_vacancyNotFound_shouldReturn404_andNotTouchDbOrKafka() {
        UUID vacancyId = UUID.randomUUID();

        when(vacancyPort.checkVacancyExists(eq(vacancyId), anyString()))
                .thenReturn(Mono.just(false));

        postCreate(vacancyId, "Hi")
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);


        StepVerifier.create(applicationRepositoryPort.existsByUserIdAndVacancyId(userId, vacancyId))
                .expectNext(false)
                .verifyComplete();

        verify(applicationEventPublisherPort, never()).publishApplicationCreate(any());
    }

    @Test
    void createApplication_vacancyNotPublished_shouldReturn400() {
        UUID vacancyId = UUID.randomUUID();

        when(vacancyPort.checkVacancyIsPublished(eq(vacancyId), anyString()))
                .thenReturn(Mono.just(false));

        postCreate(vacancyId, "Hi")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void createApplication_userHasAlreadyApplied_shouldReturn400() {
        UUID vacancyId = UUID.randomUUID();


        var newStatus = applicationStatusRepositoryPort.findByName(ApplicationStatusName.NEW).block();
        assertNotNull(newStatus);

        var now = LocalDateTime.now();
        var existing = Application.builder()
                .userId(userId)
                .vacancyId(vacancyId)
                .status(newStatus.getId())
                .coverLetter("old")
                .createdAt(now)
                .updatedAt(now)
                .build();

        applicationRepositoryPort.save(existing).block();

        postCreate(vacancyId, "Hi")
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

        verify(applicationEventPublisherPort, never()).publishApplicationCreate(any());
    }
}