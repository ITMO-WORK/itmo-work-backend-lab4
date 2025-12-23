package com.itmowork.company_service.usecase;

import com.itmowork.company_service.application.usecase.ExistsCompanyByIdUseCase;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExistsCompanyByIdUseCaseTest {

    @Mock
    CompanyRepositoryPort companyRepositoryPort;

    ExistsCompanyByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ExistsCompanyByIdUseCase(companyRepositoryPort);
    }

    @Test
    void shouldReturnTrueWhenCompanyExists() {
        UUID companyId = UUID.randomUUID();

        when(companyRepositoryPort.existsById(companyId))
                .thenReturn(Mono.just(true));

        StepVerifier.create(useCase.existsCompanyById(companyId))
                .expectNext(true)
                .verifyComplete();

        verify(companyRepositoryPort).existsById(companyId);
    }

    @Test
    void shouldReturnFalseWhenCompanyDoesNotExist() {
        UUID companyId = UUID.randomUUID();

        when(companyRepositoryPort.existsById(companyId))
                .thenReturn(Mono.just(false));

        StepVerifier.create(useCase.existsCompanyById(companyId))
                .expectNext(false)
                .verifyComplete();

        verify(companyRepositoryPort).existsById(companyId);
    }
}
