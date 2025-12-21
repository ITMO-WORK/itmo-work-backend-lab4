package org.ilestegor.applicationservice.application.port.output;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CurrentUserPort {
    Mono<CurrentUser> getCurrentUser();

    record CurrentUser(UUID userId, String token) {
    }
}
