package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyRequestDto;
import com.itmowork.company_service.adapter.out.feign.user.dto.request.UserRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.adapter.out.feign.user.dto.response.UserResponseDto;
import com.itmowork.company_service.application.port.in.CreateCompanyPort;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.CompanyStatusRepositoryPort;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.UserPort;
import com.itmowork.company_service.domain.exception.exceptions.CompanyAlreadyExistsException;
import com.itmowork.company_service.domain.exception.exceptions.UserClientException;
import com.itmowork.company_service.domain.model.Company;
import com.itmowork.company_service.domain.model.CompanyStatus;
import com.itmowork.company_service.domain.model.CompanyStatusName;
import com.itmowork.company_service.domain.model.UserCompany;
import feign.FeignException;
import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

@Service
@RequiredArgsConstructor
public class CreateCompanyUseCase implements CreateCompanyPort {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final CompanyStatusRepositoryPort companyStatusRepositoryPort;
    private final UserCompanyRepositoryPort userCompanyRepositoryPort;
    private final CircuitBreakerRegistry registry;
    private final UserPort userPort;

    @Override
    public Mono<CompanyResponseDto> createCompany(CompanyRequestDto companyRequestDto) {
        return companyRepositoryPort.existsByEmail(companyRequestDto.email())
                .flatMap(isExists -> {
                            if(isExists){
                                return Mono.error(new CompanyAlreadyExistsException("Компания с таким email уже существует"));
                            }
                            Mono<CompanyStatus> companyStatusMono = companyStatusRepositoryPort.
                                    findCompanyStatusByCompanyStatusName(CompanyStatusName.PENDING_VERIFICATION);


                            Mono<UserResponseDto> userResponseDtoMono = createRemoteUser(
                                    new UserRequestDto(
                                            companyRequestDto.ownerFullName(),
                                            companyRequestDto.ownerPassword(),
                                            companyRequestDto.ownerEmail()
                                    )
                            );

                            return Mono.zip(companyStatusMono, userResponseDtoMono);
                        }
                )
                .flatMap(tuple -> {
                    CompanyStatus companyStatus = tuple.getT1();
                    UserResponseDto userResponseDto = tuple.getT2();

                    Company company = getCompany(companyRequestDto, companyStatus);

                    return companyRepositoryPort.save(company)
                            .map(savedCompany -> Tuples.of(savedCompany, userResponseDto));
                })
                .flatMap(tuple -> {
                    Company savedCompany = tuple.getT1();
                    UserResponseDto userResponseDto = tuple.getT2();

                    UserCompany userCompany = new UserCompany();
                    userCompany.setCompanyId(savedCompany.getId());
                    userCompany.setUserId(userResponseDto.id());

                    return userCompanyRepositoryPort.saveUserCompany(userCompany);

                })
                .map(userCompany -> mapToCompanyResponseDto(companyRequestDto, userCompany));
    }

    private Company getCompany(CompanyRequestDto companyRequestDto, CompanyStatus companyStatus){
        Company company = new Company();
        company.setName(companyRequestDto.name());
        company.setEmail(companyRequestDto.email());
        company.setDescription(companyRequestDto.description());
        company.setStatusId(companyStatus.getId());
        return company;
    }

    private CompanyResponseDto mapToCompanyResponseDto(CompanyRequestDto companyRequestDto, UserCompany userCompany){
        return new CompanyResponseDto(
                userCompany.getCompanyId(),
                companyRequestDto.name(),
                companyRequestDto.email(),
                companyRequestDto.description(),
                "Компания зарегистрирована. Ожидайте проверки администратора.",
                userCompany.getUserId()
        );
    }

    public Mono<UserResponseDto> createRemoteUser(UserRequestDto userRequestDto) {
        return Mono.deferContextual(ctx -> {
            String token = ctx.getOrDefault("authToken", null);

            CircuitBreaker cb = registry.circuitBreaker("userClientCB");

            if (token == null) return Mono.error(new BadCredentialsException("Not authorized"));

            return Mono.fromCallable(() -> {
                        return userPort.registerCompanyOwner(userRequestDto, token);
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .transformDeferred(CircuitBreakerOperator.of(cb))
                    .onErrorResume(e -> {


                        if (e instanceof FeignException fe) {
                            return Mono.error(mapFeignException(fe));
                        }


                        if (isInfrastructureError(e)) {
                            return createUserFallback(userRequestDto, e);
                        }


                        return Mono.error(e);
                    });

        });
    }

    public Mono<UserResponseDto> createUserFallback(UserRequestDto dto, Throwable e) {
        return Mono.error(new UserClientException(
                "User service сейчас не доступен, создание юзера невозможно",
                HttpStatus.SERVICE_UNAVAILABLE
        ));
    }

    private boolean isInfrastructureError(Throwable e) {
        return e instanceof RetryableException
                || e instanceof CallNotPermittedException;
    }

    private RuntimeException mapFeignException(FeignException e) {
        HttpStatus status = HttpStatus.resolve(e.status());

        String message = e.contentUTF8();
        if (message == null || message.isBlank()) {
            message = "Ошибка user сервиса";
        }

        return new UserClientException(message, status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
