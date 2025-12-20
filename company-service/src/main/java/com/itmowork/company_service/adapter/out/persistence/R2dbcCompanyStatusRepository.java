package com.itmowork.company_service.adapter.out.persistence;

import com.itmowork.company_service.domain.model.CompanyStatus;
import com.itmowork.company_service.domain.model.CompanyStatusName;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface R2dbcCompanyStatusRepository extends ReactiveCrudRepository<CompanyStatus, Long> {

    Mono<CompanyStatus> findByStatus(CompanyStatusName companyStatusName);
}
