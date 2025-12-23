package com.itmowork.company_service.controller;

import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.UserResponsePayLoad;
import com.itmowork.company_service.adapter.in.web.dto.request.CompanyRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.request.CompanyUpdateRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyDeleteResponseDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.application.port.out.UserPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CompanyControllerTest extends AbstractIntegrationTest{

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @MockitoBean
    private UserPort userPort;

    @AfterEach
    void clean() {
        companyRepository
                .findAllCompaniesPaged(Long.MAX_VALUE, 0L)
                .flatMap(companyRepository::delete)
                .then()
                .block();
    }

    @BeforeEach
    void setupMocks() {
        when(userPort.registerCompanyOwner(any(), any()))
                .thenReturn(new UserResponsePayLoad(
                        USER_ID
                ));
    }

    @Test
    void shouldCreateCompany() {
        var client = withJwt(USER_ID, "user@test.com", "ROLE_ADMIN");

        CompanyRequestDto request = new CompanyRequestDto(
                "Test Company",
                "test@company.com",
                "Description",
                "Ivan Ivanov",
                "owner@test.com",
                "password123"
        );

        client.post()
                .uri("/api/company/register-company")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CompanyResponseDto.class)
                .value(response -> {
                    assertThat(response.id()).isNotNull();
                    assertThat(response.userId()).isNotNull();
                });
    }

    @Test
    void shouldUpdateCompany() {
        var client = withJwt(USER_ID, "user@test.com", "ROLE_ADMIN");


        CompanyRequestDto createRequest = new CompanyRequestDto(
                "Old Name",
                "old@company.com",
                "Old description",
                "Ivan Ivanov",
                "owner@test.com",
                "password123"
        );

        CompanyResponseDto createdCompany = client.post()
                .uri("/api/company/register-company")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CompanyResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdCompany).isNotNull();


        CompanyUpdateRequestDto updateRequest = new CompanyUpdateRequestDto(
                "New Name",
                "new@company.com",
                "New description"
        );

        client.patch()
                .uri("/api/company/update-company/{id}", createdCompany.id())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompanyResponseDto.class)
                .value(updated -> {
                    assertThat(updated.name()).isEqualTo("New Name");
                    assertThat(updated.email()).isEqualTo("new@company.com");
                });
    }

    @Test
    void shouldDeleteCompany() {
        var client = withJwt(USER_ID, "user@test.com", "ROLE_ADMIN");

        CompanyRequestDto request = new CompanyRequestDto(
                "Delete Me",
                "delete@company.com",
                "To be deleted",
                "Ivan Ivanov",
                "owner@test.com",
                "password123"
        );

        CompanyResponseDto created = client.post()
                .uri("/api/company/register-company")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CompanyResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();

        client.delete()
                .uri("/api/company/delete/{id}", created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompanyDeleteResponseDto.class)
                .value(response ->
                        assertThat(response.id()).isEqualTo(created.id())
                );

        client.get()
                .uri("/api/company/{id}", created.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void shouldGetAllCompanies() {
        var client = withJwt(USER_ID, "user@test.com", "ROLE_ADMIN");

        // создаём несколько компаний
        for (int i = 0; i < 3; i++) {
            CompanyRequestDto request = new CompanyRequestDto(
                    "Company " + i,
                    "company" + i + "@test.com",
                    "Description " + i,
                    "Ivan Ivanov",
                    "owner@test.com",
                    "password123"
            );

            client.post()
                    .uri("/api/company/register-company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }

        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/company")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(3);
    }

}
