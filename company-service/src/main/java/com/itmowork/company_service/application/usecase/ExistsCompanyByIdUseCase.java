package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.application.port.in.ExistsCompanyByIdPort;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExistsCompanyByIdUseCase implements ExistsCompanyByIdPort {

    private final CompanyRepositoryPort companyRepositoryPort;

    @Override
    public Mono<Boolean> existsCompanyById(UUID id) {
        return companyRepositoryPort.existsById(id);
    }
}
