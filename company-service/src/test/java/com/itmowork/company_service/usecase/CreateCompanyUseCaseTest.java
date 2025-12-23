package com.itmowork.company_service.usecase;

import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.UserResponsePayLoad;
import com.itmowork.company_service.adapter.in.web.dto.request.CompanyRequestDto;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.CompanyStatusRepositoryPort;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.UserPort;
import com.itmowork.company_service.application.usecase.CreateCompanyUseCase;
import com.itmowork.company_service.domain.exception.exceptions.CompanyAlreadyExistsException;
import com.itmowork.company_service.domain.model.Company;
import com.itmowork.company_service.domain.model.CompanyStatus;
import com.itmowork.company_service.domain.model.CompanyStatusName;
import com.itmowork.company_service.domain.model.UserCompany;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateCompanyUseCaseTest {
    @Mock
    CompanyRepositoryPort companyRepositoryPort;

    @Mock
    CompanyStatusRepositoryPort companyStatusRepositoryPort;

    @Mock
    UserCompanyRepositoryPort userCompanyRepositoryPort;

    @Mock
    CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    UserPort userPort;

    CreateCompanyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCompanyUseCase(
                companyRepositoryPort,
                companyStatusRepositoryPort,
                userCompanyRepositoryPort,
                circuitBreakerRegistry,
                userPort
        );
    }

    @Test
    void shouldCreateCompanySuccessfully() {
        UUID companyId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CompanyRequestDto request = new CompanyRequestDto(
                "ITMO",
                "info@itmo.ru",
                "desc",
                "Ivan Ivanov",
                "ivan@itmo.ru",
                "password"
        );

        CompanyStatus status = new CompanyStatus(1L, CompanyStatusName.PENDING_VERIFICATION);

        Company savedCompany = new Company();
        savedCompany.setId(companyId);

        UserCompany savedUserCompany = new UserCompany();
        savedUserCompany.setCompanyId(companyId);
        savedUserCompany.setUserId(userId);

        when(companyRepositoryPort.existsByEmail(request.email()))
                .thenReturn(Mono.just(false));

        when(companyStatusRepositoryPort.findCompanyStatusByCompanyStatusName(
                CompanyStatusName.PENDING_VERIFICATION))
                .thenReturn(Mono.just(status));

        CircuitBreaker cb = CircuitBreaker.ofDefaults("test");
        when(circuitBreakerRegistry.circuitBreaker(any())).thenReturn(cb);

        when(userPort.registerCompanyOwner(any(), any()))
                .thenReturn(new UserResponsePayLoad(userId));

        when(companyRepositoryPort.save(any()))
                .thenReturn(Mono.just(savedCompany));

        when(userCompanyRepositoryPort.saveUserCompany(any()))
                .thenReturn(Mono.just(savedUserCompany));

        StepVerifier.create(
                        useCase.createCompany(request)
                                .contextWrite(ctx -> ctx.put("authToken", "token"))
                )
                .assertNext(response -> {
                    assert response.id().equals(companyId);
                    assert response.userId().equals(userId);
                    assert response.name().equals("ITMO");
                })
                .verifyComplete();

        verify(companyRepositoryPort).save(any());
        verify(userCompanyRepositoryPort).saveUserCompany(any());
    }

    @Test
    void shouldFailWhenCompanyAlreadyExists() {
        CompanyRequestDto request = new CompanyRequestDto(
                "ITMO",
                "info@itmo.ru",
                "desc",
                "Ivan Ivanov",
                "ivan@itmo.ru",
                "password"
        );

        when(companyRepositoryPort.existsByEmail(request.email()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(
                        useCase.createCompany(request)
                                .contextWrite(ctx -> ctx.put("authToken", "token"))
                )
                .expectError(CompanyAlreadyExistsException.class)
                .verify();

        verify(companyRepositoryPort, never()).save(any());
        verify(userCompanyRepositoryPort, never()).saveUserCompany(any());
        verify(userPort, never()).registerCompanyOwner(any(), any());
    }

    @Test
    void shouldFailWhenNoAuthTokenProvided() {
        CompanyRequestDto request = new CompanyRequestDto(
                "ITMO",
                "info@itmo.ru",
                "desc",
                "Ivan Ivanov",
                "ivan@itmo.ru",
                "password"
        );

        when(companyRepositoryPort.existsByEmail(request.email()))
                .thenReturn(Mono.just(false));

        when(companyStatusRepositoryPort.findCompanyStatusByCompanyStatusName(
                CompanyStatusName.PENDING_VERIFICATION))
                .thenReturn(Mono.just(new CompanyStatus(1L, CompanyStatusName.PENDING_VERIFICATION)));

        StepVerifier.create(useCase.createCompany(request))
                .expectError(BadCredentialsException.class)
                .verify();

        verify(userPort, never()).registerCompanyOwner(any(), any());
    }


}
