package org.ilestegor.applicationservice.dirty.repository;


import org.ilestegor.applicationservice.dirty.model.ApplicationStatus;
import org.ilestegor.applicationservice.dirty.model.ApplicationStatusName;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ApplicationStatusRepository extends ReactiveCrudRepository<ApplicationStatus, Long> {
    Mono<ApplicationStatus> findApplicationStatusById(Long id);

    Mono<ApplicationStatus> findByApplicationStatusName(ApplicationStatusName applicationStatusName);
}
