package com.itmowork.company_service.adapter.out.persistence;

import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;
import com.itmowork.company_service.domain.model.UserCompany;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserCompanyRepositoryAdapter implements UserCompanyRepositoryPort {

    private final R2dbcUserCompanyRepository r2dbcUserCompanyRepository;

    @Override
    public Mono<UserCompany> saveUserCompany(UserCompany userCompany) {
        return r2dbcUserCompanyRepository.save(userCompany);
    }

    @Override
    public Mono<Boolean> validateCompanyOwnership(UUID companyId, UUID userId) {
        return r2dbcUserCompanyRepository.existsByCompanyIdAndUserId(companyId, userId);
    }
}
