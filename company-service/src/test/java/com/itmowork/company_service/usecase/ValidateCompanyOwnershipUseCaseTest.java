package com.itmowork.company_service.usecase;

import com.itmowork.company_service.application.usecase.ValidateCompanyOwnershipUseCase;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateCompanyOwnershipUseCaseTest {

    @Mock
    UserCompanyRepositoryPort userCompanyRepositoryPort;

    ValidateCompanyOwnershipUseCase useCase;

    UUID companyId;
    UUID userId;

    @BeforeEach
    void setUp() {
        useCase = new ValidateCompanyOwnershipUseCase(userCompanyRepositoryPort);
        companyId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void shouldReturnTrueWhenUserOwnsCompany() {
        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(true));

        StepVerifier.create(useCase.validateCompanyOwnership(companyId, userId))
                .expectNext(true)
                .verifyComplete();

        verify(userCompanyRepositoryPort)
                .validateCompanyOwnership(companyId, userId);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotOwnCompany() {
        when(userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId))
                .thenReturn(Mono.just(false));

        StepVerifier.create(useCase.validateCompanyOwnership(companyId, userId))
                .expectNext(false)
                .verifyComplete();

        verify(userCompanyRepositoryPort)
                .validateCompanyOwnership(companyId, userId);
    }
}
