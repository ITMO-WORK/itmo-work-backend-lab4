package com.itmowork.company_service.adapter.out.persistence;

import com.itmowork.company_service.domain.model.UserCompany;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface R2dbcUserCompanyRepository extends ReactiveCrudRepository<UserCompany, Long> {

    Mono<Boolean> existsByCompanyIdAndUserId(UUID companyId, UUID userId);
}
