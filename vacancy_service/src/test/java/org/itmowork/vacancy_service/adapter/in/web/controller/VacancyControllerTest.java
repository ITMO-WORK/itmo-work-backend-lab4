package org.itmowork.vacancy_service.adapter.in.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.itmowork.vacancy_service.adapter.out.kafka.company.CompanyRpcPendingRequests;
import org.itmowork.vacancy_service.adapter.out.kafka.company.dto.CompanyRequestMessage;
import org.itmowork.vacancy_service.application.port.out.VacancyEventPublisherPort;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE",
        "spring.kafka.listener.auto-startup=false",
        "spring.kafka.admin.auto-create=false",
        "spring.kafka.admin.fail-fast=false",
        "spring.kafka.bootstrap-servers=localhost:9092"
})
class VacancyControllerTest {

    private static final String TEST_JWT_SECRET =
            "72eadf75ac2a262555bdda7b35a69c9c5b42f4f4d4299efc3ca272087892f1d5";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("vacancy-db")
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
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("jwt.secret", () -> TEST_JWT_SECRET);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean KafkaTemplate<String, CompanyRequestMessage> companyRequestKafkaTemplate;
    @MockitoBean VacancyEventPublisherPort vacancyEventPublisherPort;
    @MockitoBean CompanyRpcPendingRequests pending;

    private Long currencyId;
    private Long publishedStatusId;
    private Long draftStatusId;
    private Long usdCurrencyId;

    @BeforeEach
    void setup() {
        when(pending.register(any(UUID.class)))
                .thenReturn(CompletableFuture.completedFuture(true));
        when(companyRequestKafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        currencyId = upsertCurrency("EUR");
        usdCurrencyId = upsertCurrency("USD");
        publishedStatusId = upsertVacancyStatus("PUBLISHED");
        draftStatusId = upsertVacancyStatus("DRAFT");
    }

    @Test
    @DisplayName("POST /api/vacancies/publish с валидным JWT и ролью COMPANY_OWNER -> 201 CREATED (status=PUBLISHED)")
    void createPublish_withValidJwt_shouldReturn201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "title", "Java Developer",
                "description", "Awesome vacancy",
                "salary_from", 1000,
                "salary_to", 2000,
                "company_id", companyId.toString(),
                "currency_id", currencyId
        );

        var mvcResult = mockMvc.perform(
                        post("/api/vacancies/publish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.company_id").value(companyId.toString()))
                .andExpect(jsonPath("$.status_id").value(publishedStatusId))
                .andReturn();

        UUID createdId = extractId(mvcResult.getResponse().getContentAsString());
        assertVacancyRow(createdId, companyId, currencyId, publishedStatusId, "Java Developer");
        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("POST /api/vacancies/draft с валидным JWT и ролью COMPANY_OWNER -> 201 CREATED (status=DRAFT)")
    void createDraft_withValidJwt_shouldReturn201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "title", "Draft vacancy",
                "description", "Draft description",
                "salary_from", 500,
                "salary_to", 1500,
                "company_id", companyId.toString(),
                "currency_id", currencyId
        );

        var mvcResult = mockMvc.perform(
                        post("/api/vacancies/draft")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.company_id").value(companyId.toString()))
                .andExpect(jsonPath("$.status_id").value(draftStatusId))
                .andReturn();

        UUID createdId = extractId(mvcResult.getResponse().getContentAsString());
        assertVacancyRow(createdId, companyId, currencyId, draftStatusId, "Draft vacancy");

        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("POST /api/vacancies/publish без JWT -> 401 UNAUTHORIZED")
    void createPublish_withoutJwt_shouldReturn401() throws Exception {
        UUID companyId = UUID.randomUUID();

        Map<String, Object> body = Map.of(
                "title", "Java Developer",
                "description", "Awesome vacancy",
                "salary_from", 1000,
                "salary_to", 2000,
                "company_id", companyId.toString(),
                "currency_id", currencyId
        );

        mockMvc.perform(
                        post("/api/vacancies/publish")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/change-status DRAFT -> PUBLISHED с JWT и ролью COMPANY_OWNER -> 200 OK")
    void changeStatus_withValidJwt_shouldReturn200_andUpdateDb() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        UUID vacancyId = insertVacancy(
                companyId,
                currencyId,
                draftStatusId,
                "Before",
                "Desc",
                1000,
                2000
        );
        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");
        mockMvc.perform(
                        patch("/api/vacancies/{id}/change-status", vacancyId)
                                .param("newStatus", VacancyStatusName.PUBLISHED.name())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(vacancyId.toString()))
                .andExpect(jsonPath("$.company_id").value(companyId.toString()))
                .andExpect(jsonPath("$.status_id").value(publishedStatusId));

        Long dbStatusId = jdbc.queryForObject(
                "select status_id from vacancies where id = ?",
                Long.class,
                vacancyId
        );
        assertThat(dbStatusId).isEqualTo(publishedStatusId);
        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
        verify(vacancyEventPublisherPort, times(1)).publishStatusChanged(any());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/change-status без JWT -> 401 UNAUTHORIZED")
    void changeStatus_withoutJwt_shouldReturn401() throws Exception {
        UUID vacancyId = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/vacancies/{id}/change-status", vacancyId)
                                .param("newStatus", VacancyStatusName.PUBLISHED.name())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/change-status без newStatus -> 400 BAD_REQUEST")
    void changeStatus_withoutNewStatus_shouldReturn400() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        mockMvc.perform(
                        patch("/api/vacancies/{id}/change-status", UUID.randomUUID())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update с JWT (ROLE_COMPANY_OWNER) -> 200 OK и обновляет поля в БД")
    void updateVacancy_withValidJwt_shouldReturn200_andUpdateDbFields() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        UUID vacancyId = insertVacancy(
                companyId,
                currencyId,
                draftStatusId,
                "Old title",
                "Old desc",
                1000,
                2000
        );

        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "title", "New title",
                "description", "New desc",
                "salary_from", 1500,
                "salary_to", 2500
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update", vacancyId
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(vacancyId.toString()))
                .andExpect(jsonPath("$.company_id").value(companyId.toString()))
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.description").value("New desc"))
                .andExpect(jsonPath("$.salary_from").value(1500))
                .andExpect(jsonPath("$.salary_to").value(2500))
                .andExpect(jsonPath("$.status_id").value(draftStatusId))
                .andExpect(jsonPath("$.currency_id").value(currencyId));

        var row = vacancyRow(vacancyId);
        assertThat(row.get("title")).isEqualTo("New title");
        assertThat(row.get("description")).isEqualTo("New desc");
        assertThat(row.get("salary_from")).isEqualTo(1500);
        assertThat(row.get("salary_to")).isEqualTo(2500);
        assertThat(row.get("status_id")).isEqualTo(draftStatusId);
        assertThat(row.get("currency_id")).isEqualTo(currencyId);
        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update с currency_id -> 200 OK и обновляет currency_id в БД")
    void updateVacancy_withCurrencyChange_shouldUpdateCurrency() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        UUID vacancyId = insertVacancy(
                companyId,
                currencyId,
                draftStatusId,
                "Old title",
                "Old desc",
                1000,
                2000
        );

        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "currency_id", usdCurrencyId
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update", vacancyId
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vacancyId.toString()))
                .andExpect(jsonPath("$.currency_id").value(usdCurrencyId));

        var row = vacancyRow(vacancyId);
        assertThat(row.get("currency_id")).isEqualTo(usdCurrencyId);

        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update без JWT -> 401 UNAUTHORIZED")
    void updateVacancy_withoutJwt_shouldReturn401() throws Exception {
        UUID vacancyId = UUID.randomUUID();

        Map<String, Object> body = Map.of(
                "title", "New title"
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update", vacancyId
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update с невалидным body -> 400 BAD_REQUEST")
    void updateVacancy_withInvalidBody_shouldReturn404() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "title", ""
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update", UUID.randomUUID()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update-and-change-status DRAFT -> PUBLISHED с JWT -> 200 OK, обновляет поля и статус в БД")
    void updateAndChangeStatus_withValidJwt_shouldReturn200_andUpdateDb() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        UUID vacancyId = insertVacancy(
                companyId,
                currencyId,
                draftStatusId,
                "Old title",
                "Old desc",
                1000,
                2000
        );

        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "title", "Updated & published",
                "salary_from", 1500,
                "salary_to", 2500
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update-and-change-status", vacancyId
                                )
                                .param("newStatus", VacancyStatusName.PUBLISHED.name())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(vacancyId.toString()))
                .andExpect(jsonPath("$.company_id").value(companyId.toString()))
                .andExpect(jsonPath("$.title").value("Updated & published"))
                .andExpect(jsonPath("$.salary_from").value(1500))
                .andExpect(jsonPath("$.salary_to").value(2500))
                .andExpect(jsonPath("$.status_id").value(publishedStatusId))
                .andExpect(jsonPath("$.currency_id").value(currencyId));

        var row = vacancyRow(vacancyId);
        assertThat(row.get("title")).isEqualTo("Updated & published");
        assertThat(row.get("salary_from")).isEqualTo(1500);
        assertThat(row.get("salary_to")).isEqualTo(2500);
        assertThat(row.get("status_id")).isEqualTo(publishedStatusId);
        assertThat(row.get("currency_id")).isEqualTo(currencyId);

        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
        verify(vacancyEventPublisherPort, times(1)).publishStatusChanged(any());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update-and-change-status с currency_id -> 200 OK, меняет currency_id и статус")
    void updateAndChangeStatus_withCurrencyChange_shouldUpdateCurrencyAndStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();

        UUID vacancyId = insertVacancy(
                companyId,
                currencyId,
                draftStatusId,
                "Old title",
                "Old desc",
                1000,
                2000
        );

        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "currency_id", usdCurrencyId,
                "title", "USD & published"
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update-and-change-status", vacancyId
                                )
                                .param("newStatus", VacancyStatusName.PUBLISHED.name())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vacancyId.toString()))
                .andExpect(jsonPath("$.title").value("USD & published"))
                .andExpect(jsonPath("$.currency_id").value(usdCurrencyId))
                .andExpect(jsonPath("$.status_id").value(publishedStatusId));

        var row = vacancyRow(vacancyId);
        assertThat(row.get("currency_id")).isEqualTo(usdCurrencyId);
        assertThat(row.get("status_id")).isEqualTo(publishedStatusId);

        verify(pending, times(2)).register(any(UUID.class));
        verify(companyRequestKafkaTemplate, times(2)).send(any(ProducerRecord.class));
        verify(vacancyEventPublisherPort, times(1)).publishStatusChanged(any());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update-and-change-status без JWT -> 401 UNAUTHORIZED")
    void updateAndChangeStatus_withoutJwt_shouldReturn401() throws Exception {
        UUID vacancyId = UUID.randomUUID();

        Map<String, Object> body = Map.of(
                "title", "Updated"
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update-and-change-status", vacancyId
                                )
                                .param("newStatus", VacancyStatusName.PUBLISHED.name())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/vacancies/{id}/update-and-change-status без newStatus -> 400 BAD_REQUEST")
    void updateAndChangeStatus_withoutNewStatus_shouldReturn400() throws Exception {
        UUID userId = UUID.randomUUID();
        String token = generateJwt(userId, "owner@example.com", "ROLE_COMPANY_OWNER");

        Map<String, Object> body = Map.of(
                "title", "Updated"
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                        "/api/vacancies/{id}/update-and-change-status", UUID.randomUUID()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/vacancies -> 200 OK и возвращает только PUBLISHED вакансии")
    void getAllPublishedVacancies_shouldReturnOnlyPublished() throws Exception {
        jdbc.update("delete from vacancies");

        UUID companyId = UUID.randomUUID();

        UUID published1 = insertVacancy(
                companyId, currencyId, publishedStatusId,
                "Published 1", "Desc", 1000, 2000
        );
        UUID draft1 = insertVacancy(
                companyId, currencyId, draftStatusId,
                "Draft 1", "Desc", 1000, 2000
        );
        UUID published2 = insertVacancy(
                companyId, currencyId, publishedStatusId,
                "Published 2", "Desc", 1000, 2000
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/vacancies")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.hasItems(
                        published1.toString(),
                        published2.toString()
                )))
                .andExpect(jsonPath("$.content[*].id").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(draft1.toString())
                )));
    }

    @Test
    @DisplayName("GET /api/vacancies?page=0&size=1 -> 200 OK и возвращает 1 элемент")
    void getAllPublishedVacancies_withPagination_shouldRespectSize() throws Exception {
        jdbc.update("delete from vacancies");

        UUID companyId = UUID.randomUUID();

        insertVacancy(companyId, currencyId, publishedStatusId, "Published 1", "Desc", 1000, 2000);
        insertVacancy(companyId, currencyId, publishedStatusId, "Published 2", "Desc", 1000, 2000);
        insertVacancy(companyId, currencyId, draftStatusId, "Draft 1", "Desc", 1000, 2000);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/vacancies")
                                .param("page", "0")
                                .param("size", "1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/vacancies когда нет PUBLISHED -> 200 OK и пустой content")
    void getAllPublishedVacancies_whenNoPublished_shouldReturnEmpty() throws Exception {
        jdbc.update("delete from vacancies");

        UUID companyId = UUID.randomUUID();
        insertVacancy(companyId, currencyId, draftStatusId, "Draft 1", "Desc", 1000, 2000);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/vacancies")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
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
        return UUID.fromString(node.get("id").asText());
    }

    private Long upsertCurrency(String code) {
        Long existing = jdbc.query(
                "select id from currencies where currency = ?",
                rs -> rs.next() ? rs.getLong(1) : null,
                code
        );
        if (existing != null) return existing;

        return jdbc.queryForObject(
                "insert into currencies(currency) values (?) returning id",
                Long.class,
                code
        );
    }

    private Long upsertVacancyStatus(String status) {
        Long existing = jdbc.query(
                "select id from vacancy_status where status = ?",
                rs -> rs.next() ? rs.getLong(1) : null,
                status
        );
        if (existing != null) return existing;

        return jdbc.queryForObject(
                "insert into vacancy_status(status) values (?) returning id",
                Long.class,
                status
        );
    }

    private void assertVacancyRow(UUID id, UUID companyId, Long currencyId, Long statusId, String title) {
        var row = jdbc.queryForMap("select * from vacancies where id = ?", id);

        assertThat(row.get("company_id")).isNotNull();
        assertThat(row.get("currency_id")).isEqualTo(currencyId);
        assertThat(row.get("status_id")).isEqualTo(statusId);
        assertThat(row.get("title")).isEqualTo(title);
        Object companyIdDb = row.get("company_id");
        if (companyIdDb instanceof UUID uuid) {
            assertThat(uuid).isEqualTo(companyId);
        } else {
            assertThat(companyIdDb.toString()).isEqualTo(companyId.toString());
        }
    }

    private UUID insertVacancy(UUID companyId, Long currencyId, Long statusId,
                               String title, String description, Integer salaryFrom, Integer salaryTo) {
        UUID id = UUID.randomUUID();

        jdbc.update("""
        insert into vacancies(
            id, title, description, salary_from, salary_to, created_at, company_id, status_id, currency_id
        ) values (?, ?, ?, ?, ?, now(), ?, ?, ?)
        """,
                id,
                title,
                description,
                salaryFrom,
                salaryTo,
                companyId,
                statusId,
                currencyId
        );

        return id;
    }

    private Map<String, Object> vacancyRow(UUID id) {
        return jdbc.queryForMap("select * from vacancies where id = ?", id);
    }
}
