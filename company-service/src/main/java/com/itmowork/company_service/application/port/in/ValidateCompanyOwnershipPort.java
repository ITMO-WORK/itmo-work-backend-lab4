package com.itmowork.company_service.application.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ValidateCompanyOwnershipPort {

    Mono<Boolean> validateCompanyOwnership(UUID companyId, UUID userId);
}
