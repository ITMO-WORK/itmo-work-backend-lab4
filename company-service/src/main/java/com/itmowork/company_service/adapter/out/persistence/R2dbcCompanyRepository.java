package com.itmowork.company_service.adapter.out.persistence;

import com.itmowork.company_service.domain.model.Company;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcCompanyRepository extends ReactiveCrudRepository<Company, UUID> {
    Mono<Boolean> existsByEmail(String email);

    Mono<Company> findCompanyById(UUID id);

    @Query("SELECT * FROM companies LIMIT :limit OFFSET :offset")
    Flux<Company> findAllCompaniesPaged(Long limit, Long offset);
}
