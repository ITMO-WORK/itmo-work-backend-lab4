package com.itmowork.company_service.adapter.out.persistence;

import com.itmowork.company_service.application.port.out.CompanyStatusRepositoryPort;
import com.itmowork.company_service.domain.model.CompanyStatus;
import com.itmowork.company_service.domain.model.CompanyStatusName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CompanyStatusRepositoryAdapter implements CompanyStatusRepositoryPort {

    private final R2dbcCompanyStatusRepository r2dbcCompanyStatusRepository;
    @Override
    public Mono<CompanyStatus> findCompanyStatusByCompanyStatusName(CompanyStatusName companyStatusName) {
        return r2dbcCompanyStatusRepository.findByStatus(companyStatusName)
                .switchIfEmpty(
                        Mono.error(new com.itmowork.company_service.domain.model.exception.exceptions.CompanyStatusNotFoundException("Компания с таким статусом не найдена"))
                );
    }
}
