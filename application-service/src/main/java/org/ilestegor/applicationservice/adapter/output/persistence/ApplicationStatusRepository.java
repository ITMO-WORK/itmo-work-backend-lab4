package org.ilestegor.applicationservice.adapter.output.persistence;


import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ApplicationStatusRepository extends ReactiveCrudRepository<ApplicationStatus, Long> {
    Mono<ApplicationStatus> findApplicationStatusById(Long id);

    Mono<ApplicationStatus> findByApplicationStatusName(ApplicationStatusName applicationStatusName);
}
