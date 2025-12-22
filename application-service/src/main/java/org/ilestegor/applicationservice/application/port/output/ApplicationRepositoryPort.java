package org.ilestegor.applicationservice.application.port.output;

import org.ilestegor.applicationservice.domain.Application;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApplicationRepositoryPort {
    Mono<Boolean> existsByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    Mono<Application> findApplicationsByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    Mono<Application> findById(UUID applicationId);

    Flux<Application> findAllByVacancyId(UUID vacancyId, Pageable pageable);

    Mono<Long> countApplicationByVacancyId(UUID vacancyId);

    Mono<UUID> findVacancyIdById(UUID applicationId);

    Mono<Boolean> existsById(UUID id);

    Mono<Integer> updateFileId(UUID id, UUID fileId);

    Mono<Application> save(Application application);

    Mono<Void> updateFileIdByApplicationId(UUID applicationId, UUID fieldId);

    Mono<Application> findApplicationIdByUserId(UUID userId);

    Mono<Void> updateStatus(UUID applicationId, Long statusId);


}
