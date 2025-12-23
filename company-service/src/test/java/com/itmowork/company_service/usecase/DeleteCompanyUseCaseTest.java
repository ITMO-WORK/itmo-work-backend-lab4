package com.itmowork.company_service.usecase;

import com.itmowork.company_service.application.usecase.DeleteCompanyUseCase;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import com.itmowork.company_service.configuration.UserPrincipal;
import com.itmowork.company_service.domain.exception.exceptions.CompanyNotFoundException;
import com.itmowork.company_service.domain.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCompanyUseCaseTest {

    @Mock
    CompanyRepositoryPort companyRepositoryPort;

    @Mock
    UserCompanyRepositoryPort userCompanyRepositoryPort;

    DeleteCompanyUseCase useCase;

    UUID companyId;
    UUID userId;
    String email;

    @BeforeEach
    void setUp() {
        useCase = new DeleteCompanyUseCase(
                companyRepositoryPort,
                userCompanyRepositoryPort
        );

        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
        email = "test@mail.com";
    }

    @Test
    void shouldDeleteCompanySuccessfully() {
        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        when(companyRepositoryPort.findCompanyById(companyId))
                .thenReturn(Mono.just(new Company()));

        when(companyRepositoryPort.delete(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        useCase.deleteCompany(companyId)
                                .contextWrite(securityContext())
                )
                .assertNext(response -> {
                    assert response.id().equals(companyId);
                    assert response.message().contains("успешно");
                })
                .verifyComplete();

        verify(companyRepositoryPort).delete(any());
    }

    @Test
    void shouldFailWhenCompanyDoesNotBelongToUser() {
        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(false));

        StepVerifier.create(
                        useCase.deleteCompany(companyId)
                                .contextWrite(securityContext())
                )
                .expectError(CompanyNotFoundException.class)
                .verify();

        verify(companyRepositoryPort, never()).delete(any());
    }

    @Test
    void shouldFailWhenNoSecurityContext() {
        StepVerifier.create(useCase.deleteCompany(companyId))
                .expectError(CompanyNotFoundException.class)
                .verify();
    }



    private Context securityContext() {
        UserPrincipal principal = new UserPrincipal(email, userId);
        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, null);

        return ReactiveSecurityContextHolder.withAuthentication(auth);
    }
}
