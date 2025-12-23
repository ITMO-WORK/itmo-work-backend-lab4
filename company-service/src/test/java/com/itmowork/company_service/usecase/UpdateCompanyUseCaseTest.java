package com.itmowork.company_service.usecase;

import com.itmowork.company_service.application.usecase.UpdateCompanyUseCase;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import com.itmowork.company_service.adapter.in.web.dto.request.CompanyUpdateRequestDto;
import com.itmowork.company_service.configuration.UserPrincipal;
import com.itmowork.company_service.domain.exception.exceptions.CompanyNotFoundException;
import com.itmowork.company_service.domain.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCompanyUseCaseTest {

    @Mock
    CompanyRepositoryPort companyRepositoryPort;

    @Mock
    UserCompanyRepositoryPort userCompanyRepositoryPort;

    UpdateCompanyUseCase useCase;

    UUID companyId;
    UUID userId;
    String email;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCompanyUseCase(companyRepositoryPort, userCompanyRepositoryPort);
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        email = "test@mail.com";
    }

    @Test
    void shouldUpdateCompanySuccessfully() {
        Company existing = new Company();
        existing.setId(companyId);
        existing.setName("Old name");
        existing.setEmail("old@mail.com");
        existing.setDescription("Old desc");

        Company saved = new Company();
        saved.setId(companyId);
        saved.setName("New name");
        saved.setEmail("new@mail.com");
        saved.setDescription("New desc");

        CompanyUpdateRequestDto request =
                new CompanyUpdateRequestDto("New name", "new@mail.com", "New desc");

        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        when(companyRepositoryPort.findCompanyById(companyId))
                .thenReturn(Mono.just(existing));

        when(companyRepositoryPort.save(any()))
                .thenReturn(Mono.just(saved));

        StepVerifier.create(
                        useCase.updateCompany(companyId, request)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        new UsernamePasswordAuthenticationToken(
                                                new UserPrincipal(email, userId),
                                                null
                                        )
                                ))
                )
                .assertNext(dto -> {
                    assertThat(dto.id()).isEqualTo(companyId);
                    assertThat(dto.name()).isEqualTo("New name");
                    assertThat(dto.email()).isEqualTo("new@mail.com");
                    assertThat(dto.description()).isEqualTo("New desc");
                    assertThat(dto.userId()).isEqualTo(userId);
                    assertThat(dto.statusMessage())
                            .isEqualTo("Компания была успешно обновлена");
                })
                .verifyComplete();

        verify(companyRepositoryPort).save(any());
    }

    @Test
    void shouldFailWhenCompanyDoesNotBelongToUser() {
        CompanyUpdateRequestDto request =
                new CompanyUpdateRequestDto("New name", null, null);

        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(false));

        StepVerifier.create(
                        useCase.updateCompany(companyId, request)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        new UsernamePasswordAuthenticationToken(
                                                new UserPrincipal(email, userId),
                                                null
                                        )
                                ))
                )
                .expectError(CompanyNotFoundException.class)
                .verify();

        verify(companyRepositoryPort, never()).findCompanyById(any());
        verify(companyRepositoryPort, never()).save(any());
    }

    @Test
    void shouldFailWhenCompanyNotFound() {
        CompanyUpdateRequestDto request =
                new CompanyUpdateRequestDto("New name", null, null);

        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        when(companyRepositoryPort.findCompanyById(companyId))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        useCase.updateCompany(companyId, request)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        new UsernamePasswordAuthenticationToken(
                                                new UserPrincipal(email, userId),
                                                null
                                        )
                                ))
                )
                .expectError(CompanyNotFoundException.class)
                .verify();

        verify(companyRepositoryPort, never()).save(any());
    }

    @Test
    void shouldNotUpdateNameWhenNameIsNull() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Company existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setName("Old name");
        existingCompany.setEmail("old@email.com");
        existingCompany.setDescription("Old desc");

        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto(
                null,
                "new@email.com",
                "new desc"
        );

        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        when(companyRepositoryPort.findCompanyById(companyId))
                .thenReturn(Mono.just(existingCompany));

        when(companyRepositoryPort.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(
                        useCase.updateCompany(companyId, request)
                                .contextWrite(ctx ->
                                        ReactiveSecurityContextHolder.withAuthentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        new UserPrincipal(email, userId),
                                                        null,
                                                        List.of()
                                                )
                                        )
                                )
                )
                .assertNext(response -> {
                    assert response.name().equals("Old name");
                    assert response.email().equals("new@email.com");
                    assert response.description().equals("new desc");
                })
                .verifyComplete();
    }

    @Test
    void shouldNotUpdateEmailWhenEmailIsNull() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);
        company.setName("Company");
        company.setEmail("old@email.com");
        company.setDescription("desc");

        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto(
                "New name",
                null,
                "new desc"
        );

        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        when(companyRepositoryPort.findCompanyById(companyId))
                .thenReturn(Mono.just(company));

        when(companyRepositoryPort.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(
                        useCase.updateCompany(companyId, request)
                                .contextWrite(ctx ->
                                        ReactiveSecurityContextHolder.withAuthentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        new UserPrincipal(email, userId),
                                                        null,
                                                        List.of()
                                                )
                                        )
                                )
                )
                .assertNext(resp -> {
                    assert resp.email().equals("old@email.com");
                    assert resp.name().equals("New name");
                })
                .verifyComplete();
    }

    @Test
    void shouldNotUpdateDescriptionWhenDescriptionIsNull() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Company company = new Company();
        company.setId(companyId);
        company.setName("Company");
        company.setEmail("mail@mail.com");
        company.setDescription("old desc");

        CompanyUpdateRequestDto request = new CompanyUpdateRequestDto(
                "New name",
                "new@mail.com",
                null
        );

        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        when(companyRepositoryPort.findCompanyById(companyId))
                .thenReturn(Mono.just(company));

        when(companyRepositoryPort.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(
                        useCase.updateCompany(companyId, request)
                                .contextWrite(ctx ->
                                        ReactiveSecurityContextHolder.withAuthentication(
                                                new UsernamePasswordAuthenticationToken(
                                                        new UserPrincipal(email, userId),
                                                        null,
                                                        List.of()
                                                )
                                        )
                                )
                )
                .assertNext(resp -> {
                    assert resp.description().equals("old desc");
                    assert resp.name().equals("New name");
                })
                .verifyComplete();
    }



}
