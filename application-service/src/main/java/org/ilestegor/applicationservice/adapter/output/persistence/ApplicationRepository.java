package org.ilestegor.applicationservice.adapter.output.persistence;

import org.ilestegor.applicationservice.domain.Application;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApplicationRepository extends ReactiveCrudRepository<Application, Long>{
    Mono<Boolean> existsByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    Mono<Application> findApplicationsByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    Mono<Application> findById(UUID applicationId);

    Flux<Application> findAllByVacancyId(UUID vacancyId, Pageable pageable);

    Mono<Long> countApplicationByVacancyId(UUID vacancyId);

    @Query("select vacancy_id from applications where id =:applicationId ")
    Mono<UUID> findVacancyIdById(@Param("applicationId") UUID applicationId);

    Mono<Boolean> existsById(UUID id);

    @Query("UPDATE applications SET file_id = :fileId, updated_at = NOW() WHERE id = :id")
    Mono<Integer> updateFileId(UUID id, UUID fileId);

    @Query("UPDATE applications SET file_id = :fileId, updated_at = NOW() WHERE id = :applicationId")
    Mono<Void> updateFileIdById(UUID applicationId, UUID fileId);

    Mono<UUID> findApplicationByUserId(UUID userId);
}
