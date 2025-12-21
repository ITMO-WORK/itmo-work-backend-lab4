package com.itmowork.company_service.application.port.out;

import com.itmowork.company_service.domain.model.Company;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CompanyRepositoryPort {

    Mono<Boolean> existsByEmail(String email);

    Mono<Company> findCompanyById(UUID id);

    Flux<Company> findAllCompaniesPaged(Long limit, Long offset);

    Mono<Company> save(Company company);

    Mono<Void> delete(Company company);

    Mono<Long> count();
    Mono<Boolean> existsById(UUID id);

}
