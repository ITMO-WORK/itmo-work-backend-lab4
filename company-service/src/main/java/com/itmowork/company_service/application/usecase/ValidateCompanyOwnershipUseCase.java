package com.itmowork.company_service.application.usecase;

import com.itmowork.company_service.application.port.in.ValidateCompanyOwnershipPort;
import com.itmowork.company_service.application.port.out.UserCompanyRepositoryPort;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidateCompanyOwnershipUseCase implements ValidateCompanyOwnershipPort {

    private final UserCompanyRepositoryPort userCompanyRepositoryPort;

    @Override
    public Mono<Boolean> validateCompanyOwnership(UUID companyId, UUID userId) {
        return userCompanyRepositoryPort.validateCompanyOwnership(companyId, userId);
    }
}
