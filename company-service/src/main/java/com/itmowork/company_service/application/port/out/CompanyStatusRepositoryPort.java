package com.itmowork.company_service.application.port.out;

import com.itmowork.company_service.domain.model.CompanyStatus;
import com.itmowork.company_service.domain.model.CompanyStatusName;
import reactor.core.publisher.Mono;

public interface CompanyStatusRepositoryPort {

    Mono<CompanyStatus> findCompanyStatusByCompanyStatusName(CompanyStatusName companyStatusName);
}
