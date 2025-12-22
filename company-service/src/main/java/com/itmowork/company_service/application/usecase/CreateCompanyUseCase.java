package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyRequestDto;
import com.itmowork.company_service.adapter.out.kafka.user.dto.UserRequestPayLoad;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.UserResponsePayLoad;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateCompanyUseCase implements CreateCompanyPort {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final CompanyStatusRepositoryPort companyStatusRepositoryPort;
    private final UserCompanyRepositoryPort userCompanyRepositoryPort;
    private final CircuitBreakerRegistry registry;
    private final UserPort userPort;

    @Override
    @Transactional
    public Mono<CompanyResponseDto> createCompany(CompanyRequestDto companyRequestDto) {
        return companyRepositoryPort.existsByEmail(companyRequestDto.email())
                .flatMap(isExists -> {
                            if(isExists){
                                return Mono.error(new CompanyAlreadyExistsException("Компания с таким email уже существует"));
                            }
                            Mono<CompanyStatus> companyStatusMono = companyStatusRepositoryPort.
                                    findCompanyStatusByCompanyStatusName(CompanyStatusName.PENDING_VERIFICATION);


                            Mono<UserResponsePayLoad> userResponseDtoMono = createRemoteUser(
                                    new UserRequestPayLoad(
                                            companyRequestDto.ownerFullName(),
                                            companyRequestDto.ownerEmail(),
                                            companyRequestDto.ownerPassword()
                                    )
                            );

                            return Mono.zip(companyStatusMono, userResponseDtoMono);
                        }
                )
                .flatMap(tuple -> {
                    CompanyStatus companyStatus = tuple.getT1();
                    UserResponsePayLoad userResponsePayLoad = tuple.getT2();

                    log.info(userResponsePayLoad + "");

                    Company company = getCompany(companyRequestDto, companyStatus);

                    return companyRepositoryPort.save(company)
                            .map(savedCompany -> Tuples.of(savedCompany, userResponsePayLoad));
                })
                .flatMap(tuple -> {
                    Company savedCompany = tuple.getT1();
                    UserResponsePayLoad userResponsePayLoad = tuple.getT2();

                    UserCompany userCompany = new UserCompany();
                    userCompany.setCompanyId(savedCompany.getId());
                    userCompany.setUserId(userResponsePayLoad.userId());

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

    public Mono<UserResponsePayLoad> createRemoteUser(UserRequestPayLoad userRequestPayLoad) {
        return Mono.deferContextual(ctx -> {
            log.info("Reactor context keys: {}", ctx.stream().map(e -> e.getKey()).toList());

            String token = ctx.getOrDefault("authToken", null);
            log.info("Auth token from context = {}", token);
            if (token == null) {
                return Mono.error(new BadCredentialsException("Not authorized"));
            }

            CircuitBreaker cb = registry.circuitBreaker("userClientCB");

            return Mono.fromCallable(() ->
                            userPort.registerCompanyOwner(userRequestPayLoad, token)
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .transformDeferred(CircuitBreakerOperator.of(cb))
                    .onErrorMap(this::mapInfrastructureErrors);
        });
    }

    private Throwable mapInfrastructureErrors(Throwable e) {

        if (e instanceof TimeoutException
                || e instanceof RetryableException
                || e instanceof CallNotPermittedException) {

            return new UserClientException(
                    "User service сейчас не доступен, создание юзера невозможно",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        return e;
    }
}
