package com.itmowork.company_service.application.port.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ExistsCompanyByIdPort {
    Mono<Boolean> existsCompanyById(UUID id);
}
