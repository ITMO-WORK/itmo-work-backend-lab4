package org.ilestegor.applicationservice.application.port.output;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CompanyPort {
    Mono<Boolean> isUserBelongsToCompany(UUID companyId, UUID userId, String token);
}
