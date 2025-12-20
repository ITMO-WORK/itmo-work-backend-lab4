package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.adapter.in.web.dto.request.CompanyUpdateRequestDto;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.application.port.in.UpdateCompanyPort;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import com.itmowork.company_service.configuration.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCompanyUseCase implements UpdateCompanyPort {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final UserCompanyRepositoryPort userCompanyRepositoryPort;

    @Override
    public Mono<CompanyResponseDto> updateCompany(UUID id, CompanyUpdateRequestDto companyUpdateRequestDto) {
        return getUserId()
                .flatMap(userId ->
                        userCompanyRepositoryPort.validateCompanyOwnership(id, userId)
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(
                                        new com.itmowork.company_service.domain.model.exception.exceptions.CompanyNotFoundException("Компания не принадлежит пользователю")
                                ))
                                .flatMap(valid -> companyRepositoryPort.findCompanyById(id))
                                .switchIfEmpty(Mono.error(
                                        new com.itmowork.company_service.domain.model.exception.exceptions.CompanyNotFoundException("Компания не найдена")
                                ))
                                .flatMap(company -> {

                                    if (companyUpdateRequestDto.name() != null) company.setName(companyUpdateRequestDto.name());
                                    if (companyUpdateRequestDto.email() != null) company.setEmail(companyUpdateRequestDto.email());
                                    if (companyUpdateRequestDto.description() != null) company.setDescription(companyUpdateRequestDto.description());

                                    return companyRepositoryPort.save(company);
                                })
                                .map(saved -> CompanyResponseDto.builder()
                                        .id(saved.getId())
                                        .name(saved.getName())
                                        .email(saved.getEmail())
                                        .description(saved.getDescription())
                                        .statusMessage("Компания была успешно обновлена")
                                        .userId(userId)
                                        .build()
                                )
                );
    }

    private Mono<UUID> getUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (UserPrincipal) auth.getPrincipal())
                .map(UserPrincipal::userId);
    }
}
