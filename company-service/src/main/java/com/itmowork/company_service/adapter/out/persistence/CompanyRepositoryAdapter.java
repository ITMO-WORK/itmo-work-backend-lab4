package com.itmowork.company_service.adapter.out.persistence;

import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.domain.model.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyRepositoryAdapter implements CompanyRepositoryPort {

    private final R2dbcCompanyRepository r2dbcCompanyRepository;

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return r2dbcCompanyRepository.existsByEmail(email);
    }

    @Override
    public Mono<Company> findCompanyById(UUID id) {
        return r2dbcCompanyRepository.findCompanyById(id);
    }

    @Override
    public Flux<Company> findAllCompaniesPaged(Long limit, Long offset) {
        return r2dbcCompanyRepository.findAllCompaniesPaged(limit, offset);
    }

    @Override
    public Mono<Company> save(Company company) {
        return r2dbcCompanyRepository.save(company);
    }

    @Override
    public Mono<Void> delete(Company company) {
        return r2dbcCompanyRepository.delete(company);
    }

    @Override
    public Mono<Long> count() {
        return r2dbcCompanyRepository.count();
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return r2dbcCompanyRepository.existsById(id);
    }
}
