//package org.ilestegor.applicationservice.controller;
//
//
//import org.ilestegor.applicationservice.dto.request.ApplicationCreateRequestDto;
//import org.ilestegor.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
//import org.ilestegor.applicationservice.dto.response.ApplicationCreateResponseDto;
//import org.ilestegor.applicationservice.domain.Application;
//import org.ilestegor.applicationservice.domain.ApplicationStatusName;
//import org.ilestegor.applicationservice.repository.ApplicationStatusRepository;
//import org.ilestegor.applicationservice.repository.ApplicationRepository;
//import org.ilestegor.applicationservice.security.interfaces.JwtService;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.client.MultipartBodyBuilder;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
//import static com.github.tomakehurst.wiremock.client.WireMock.*;
//import static org.assertj.core.api.Assertions.assertThat;
//
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ExtendWith(SpringExtension.class)
//@Testcontainers
//@AutoConfigureWireMock(port = 0)
//@TestPropertySource(properties = {
//        "spring.cloud.openfeign.client.config.user-service.url=http://localhost:${wiremock.server.port}",
//        "spring.cloud.openfeign.client.config.vacancy-service.url=http://localhost:${wiremock.server.port}",
//        "spring.cloud.openfeign.client.config.company-service.url=http://localhost:${wiremock.server.port}",
//
//        "spring.cloud.discovery.enabled=false",
//        "eureka.client.enabled=false"
//})
//public class ApplicationServiceControllerTest {
//    private static final String TEST_JWT_SECRET =
//            "VGhpcy1pcy1hLWxvbmcgc2VjcmV0IGtleSBmb3IgdGVzdHM=";
//
//    @Container
//    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
//            .withDatabaseName("application-db")
//            .withUsername("test")
//            .withPassword("test");
//
//    @DynamicPropertySource
//    static void dbProps(DynamicPropertyRegistry registry) {
//        registry.add("spring.r2dbc.url", () ->
//                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
//        registry.add("spring.r2dbc.username", postgres::getUsername);
//        registry.add("spring.r2dbc.password", postgres::getPassword);
//
//
//        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
//        registry.add("spring.liquibase.user", postgres::getUsername);
//        registry.add("spring.liquibase.password", postgres::getPassword);
//
//        registry.add("jwt.secret",() -> TEST_JWT_SECRET);
//    }
//
//    @AfterEach
//    void tearDown() {
//        applicationRepository.deleteAll().block();
//    }
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @Autowired
//    private ApplicationStatusRepository applicationStatusRepository;
//
//    @Autowired
//    private ApplicationRepository applicationRepository;
//
//    @Autowired
//    private JwtService jwtService;
//
//    @MockitoBean
//    private ApplicationEventPublisher applicationEventPublisher;
//
//    private String generateJwt(UUID userId, String email, String... roles) {
//        var authorities = Arrays.stream(roles)
//                .map(SimpleGrantedAuthority::new)
//                .toList();
//
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//                email,
//                null,
//                authorities
//        );
//
//        return jwtService.generateAccessToken(
//                auth,
//                userId,
//                List.of(roles)
//        );
//    }
//
//
//    @Test
//    void createApplication_shouldReturn201AndPersistToDb() {
//        UUID TEST_USER_ID = UUID.randomUUID();
//        UUID vacancyId = UUID.randomUUID();
//
//        stubFor(get(urlEqualTo("/api/user/" + TEST_USER_ID))
//                .willReturn(okJson("""
//            {
//              "id": "%s",
//              "full_name": "Test User",
//              "email": "test@mail.com"
//            }
//            """.formatted(TEST_USER_ID))));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/exists"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/is-published"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/title"))
//                .willReturn(okJson("\"Java Developer\"")));
//
//        String jwt = generateJwt(TEST_USER_ID, "test@mail.com", "ROLE_USER");
//
//        ApplicationCreateRequestDto request =
//                new ApplicationCreateRequestDto("cover letter");
//
//        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
//
//        bodyBuilder
//                .part("data", request)
//                .contentType(MediaType.APPLICATION_JSON);
//
//        var entity = webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application")
//                        .queryParam("vacancyId", vacancyId)
//                        .build()
//                )
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody(ApplicationCreateResponseDto.class)
//                .returnResult()
//                .getResponseBody();
//
//        assertThat(entity).isNotNull();
//        assertThat(entity.id()).isNotNull();
//        assertThat(entity.status()).isEqualTo(ApplicationStatusName.NEW.getValue());
//        assertThat(entity.coverLetter()).isEqualTo("cover letter");
//
//        var saved = applicationRepository.findById(entity.id()).block();
//        assertThat(saved).isNotNull();
//        assertThat(saved.getUserId()).isEqualTo(TEST_USER_ID);
//        assertThat(saved.getVacancyId()).isEqualTo(vacancyId);
//    }
//
//
//
//
//    @Test
//    void createApplication_shouldReturn404_whenUserNotFound() {
//        UUID TEST_USER_ID = UUID.randomUUID();
//        UUID vacancyId = UUID.randomUUID();
//
//        stubFor(get(urlEqualTo("/api/user/" + TEST_USER_ID))
//                .willReturn(aResponse().withStatus(404)));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/exists"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/is-published"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/title"))
//                .willReturn(okJson("\"Java Developer\"")));
//
//        String jwt = generateJwt(TEST_USER_ID, "test@mail.com", "ROLE_USER");
//
//        ApplicationCreateRequestDto request =
//                new ApplicationCreateRequestDto("cover letter");
//
//        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
//        bodyBuilder
//                .part("data", request)
//                .contentType(MediaType.APPLICATION_JSON);
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application")
//                        .queryParam("vacancyId", vacancyId)
//                        .build()
//                )
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
//                .exchange()
//                .expectStatus().isNotFound()
//                .expectBody()
//                .jsonPath("$.status").isEqualTo(404);
//    }
//
//
//    @Test
//    void createApplication_shouldReturn404_whenVacancyNotFound() {
//        UUID TEST_USER_ID = UUID.randomUUID();
//        UUID vacancyId = UUID.randomUUID();
//
//        stubFor(get(urlEqualTo("/api/user/" + TEST_USER_ID))
//                .willReturn(okJson("""
//            {
//              "id": "%s",
//              "full_name": "Test User",
//              "email": "test@mail.com"
//            }
//            """.formatted(TEST_USER_ID))));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/exists"))
//                .willReturn(okJson("false")));
//
//        String jwt = generateJwt(TEST_USER_ID, "test@mail.com", "ROLE_USER");
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application")
//                        .queryParam("vacancyId", vacancyId)
//                        .build()
//                )
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(
//                        MultipartBodyBuilderBuilder.applicationCreate("cover letter")
//                )
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//
//    @Test
//    void createApplication_shouldReturn400_whenApplicationAlreadyExists() {
//        UUID TEST_USER_ID = UUID.randomUUID();
//        UUID vacancyId = UUID.randomUUID();
//
//        stubFor(get(urlEqualTo("/api/user/" + TEST_USER_ID))
//                .willReturn(okJson("""
//            {
//              "id": "%s",
//              "full_name": "Test User",
//              "email": "test@mail.com"
//            }
//            """.formatted(TEST_USER_ID))));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/exists"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/is-published"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/title"))
//                .willReturn(okJson("\"Java Developer\"")));
//
//        var newStatus = applicationStatusRepository
//                .findByApplicationStatusName(ApplicationStatusName.NEW)
//                .blockOptional()
//                .orElseThrow(() -> new IllegalStateException("Status NEW not found"));
//
//        Application existing = new Application();
//        existing.setUserId(TEST_USER_ID);
//        existing.setVacancyId(vacancyId);
//        existing.setCoverLetter("already applied");
//        existing.setStatus(newStatus.getId());
//
//        applicationRepository.save(existing).block();
//
//        String jwt = generateJwt(TEST_USER_ID, "test@mail.com", "ROLE_USER");
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application")
//                        .queryParam("vacancyId", vacancyId)
//                        .build()
//                )
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(
//                        MultipartBodyBuilderBuilder.applicationCreate("cover letter")
//                )
//                .exchange()
//                .expectStatus().isBadRequest();
//
//        var all = applicationRepository.findAll().collectList().block();
//        assertThat(all).hasSize(1);
//    }
//
//
//    @Test
//    void createApplication_shouldReturn400_whenVacancyNotPublished() {
//        UUID TEST_USER_ID = UUID.randomUUID();
//        UUID vacancyId = UUID.randomUUID();
//
//        stubFor(get(urlEqualTo("/api/user/" + TEST_USER_ID))
//                .willReturn(okJson("""
//            {
//              "id": "%s",
//              "email": "john.doe@example.com",
//              "full_name": "John Doe"
//            }
//            """.formatted(TEST_USER_ID))));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/exists"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/is-published"))
//                .willReturn(okJson("false")));
//
//        String jwt = generateJwt(TEST_USER_ID, "test@mail.com", "ROLE_USER");
//
//        webTestClient.post()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application")
//                        .queryParam("vacancyId", vacancyId)
//                        .build()
//                )
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .body(
//                        MultipartBodyBuilderBuilder.applicationCreate(
//                                "my awesome cover letter"
//                        )
//                )
//                .exchange()
//                .expectStatus().isBadRequest();
//
//        var all = applicationRepository.findAll().collectList().block();
//        assertThat(all).isEmpty();
//    }
//
//
//
//    @Test
//    void updateApplication_shouldReturn200AndUpdateCoverLetter() {
//        UUID TEST_USER_ID = UUID.randomUUID();
//        UUID vacancyId = UUID.randomUUID();
//
//        stubFor(get(urlEqualTo("/api/user/" + TEST_USER_ID))
//                .willReturn(okJson("""
//                {
//                  "id": "%s",
//                  "full_name": "Test User",
//                  "email": "test@mail.com"
//                }
//                """.formatted(TEST_USER_ID))));
//
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/exists"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/is-published"))
//                .willReturn(okJson("true")));
//
//        stubFor(get(urlEqualTo("/api/vacancies/" + vacancyId + "/title"))
//                .willReturn(okJson("\"Java Developer\"")));
//
//        Application existing = new Application();
//        existing.setUserId(TEST_USER_ID);
//        existing.setVacancyId(vacancyId);
//        existing.setCoverLetter("old cover letter");
//        existing.setStatus(1L);
//
//        existing = applicationRepository.save(existing).block();
//        assertThat(existing).isNotNull();
//
//
//        var request = new ApplicationCreateRequestDto("updated cover letter");
//        String jwt = generateJwt(TEST_USER_ID, "test@mail.com", "ROLE_USER");
//
//        var response = webTestClient.patch()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application/{vacancyId}")
//                        .build(vacancyId)
//                )
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .exchange()
//
//                .expectStatus().isOk()
//                .expectBody(ApplicationCreateResponseDto.class)
//                .returnResult()
//                .getResponseBody();
//
//
//        assertThat(response).isNotNull();
//        assertThat(response.id()).isEqualTo(existing.getId());
//        assertThat(response.coverLetter()).isEqualTo("updated cover letter");
//        assertThat(response.status()).isEqualTo(ApplicationStatusName.NEW.getValue());
//
//
//        var all = applicationRepository.findAll().collectList().block();
//        assertThat(all).hasSize(1);
//        var saved = all.getFirst();
//
//        assertThat(saved.getId()).isEqualTo(existing.getId());
//        assertThat(saved.getUserId()).isEqualTo(TEST_USER_ID);
//        assertThat(saved.getVacancyId()).isEqualTo(vacancyId);
//        assertThat(saved.getCoverLetter()).isEqualTo("updated cover letter");
//    }
//
//
//    @Test
//    void updateApplicationStatus_shouldReturn404_whenApplicationNotFound() {
//        UUID userId = UUID.randomUUID();
//        UUID applicationId = UUID.randomUUID();
//
//        ApplicationStatusUpdateRequestDto request =
//                new ApplicationStatusUpdateRequestDto(ApplicationStatusName.REJECTED);
//        String jwt = generateJwt(userId, "test@mail.com", "ROLE_MANAGER");
//        webTestClient.patch()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/application/{applicationId}/status")
//                        .build(applicationId)
//                )
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//}
