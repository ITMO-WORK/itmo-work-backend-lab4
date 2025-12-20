package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.adapter.in.web.dto.response.CompanyDeleteResponseDto;
import com.itmowork.company_service.application.port.in.DeleteCompanyPort;
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
public class DeleteCompanyUseCase implements DeleteCompanyPort {

    private final CompanyRepositoryPort companyRepositoryPort;
    private final UserCompanyRepositoryPort userCompanyRepositoryPort;
    @Override
    public Mono<CompanyDeleteResponseDto> deleteCompany(UUID id) {
        return getUserId()
                .flatMap(userId -> userCompanyRepositoryPort.validateCompanyOwnership(id, userId))
                .filter(Boolean::booleanValue)
                .switchIfEmpty(
                        Mono.error(new com.itmowork.company_service.domain.model.exception.exceptions.CompanyNotFoundException(
                                "Компания с таким пользователем не найдена"
                        )))
                .flatMap(valid -> companyRepositoryPort.findCompanyById(id))
                .flatMap(companyRepositoryPort::delete)
                .thenReturn(new CompanyDeleteResponseDto(
                        id,
                        "Компания была успешно удалена"
                ));
    }

    private Mono<UUID> getUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(auth -> (UserPrincipal) auth.getPrincipal())
                .map(UserPrincipal::userId);
    }
}
