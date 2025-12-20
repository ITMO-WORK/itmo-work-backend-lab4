package org.ilestegor.applicationservice.application.port.output;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VacancyPort {

    Mono<Boolean> checkVacancyExists(UUID vacancyId, String token);

    Mono<Boolean> checkVacancyIsPublished(UUID vacancyId, String token);

    Mono<String> getVacancyTitle(UUID vacancyId, String token);

    Mono<UUID> getCompanyIdByVacancyId(UUID vacancyId, String token);
}
