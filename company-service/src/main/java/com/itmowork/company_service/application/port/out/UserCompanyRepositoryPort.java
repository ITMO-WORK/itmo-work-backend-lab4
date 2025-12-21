package com.itmowork.company_service.application.port.out;

import com.itmowork.company_service.domain.model.UserCompany;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserCompanyRepositoryPort {

    Mono<UserCompany> saveUserCompany(UserCompany userCompany);

    Mono<Boolean> validateCompanyOwnership(UUID companyId, UUID userId);
}
