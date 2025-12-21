package org.ilestegor.applicationservice.application.port.output;

import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import reactor.core.publisher.Mono;

public interface ApplicationStatusRepositoryPort {
    Mono<ApplicationStatus> findById(Long id);

    Mono<ApplicationStatus> findByName(ApplicationStatusName name);
}
