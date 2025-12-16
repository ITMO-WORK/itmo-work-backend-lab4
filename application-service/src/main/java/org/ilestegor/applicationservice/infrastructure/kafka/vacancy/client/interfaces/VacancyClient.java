package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.client.interfaces;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VacancyClient {
    Mono<Boolean> isPublished(UUID vacancyId);

    Mono<Boolean> exists(UUID vacancyId);

}
