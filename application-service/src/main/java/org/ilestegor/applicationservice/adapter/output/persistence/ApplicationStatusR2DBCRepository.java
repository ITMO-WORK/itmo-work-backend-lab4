package org.ilestegor.applicationservice.adapter.output.persistence;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.application.port.output.ApplicationStatusRepositoryPort;
import org.ilestegor.applicationservice.domain.ApplicationStatus;
import org.ilestegor.applicationservice.domain.ApplicationStatusName;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ApplicationStatusR2DBCRepository implements ApplicationStatusRepositoryPort {
    private final ApplicationStatusRepository applicationStatusRepository;

    @Override
    public Mono<ApplicationStatus> findById(Long id) {
        return applicationStatusRepository.findApplicationStatusById(id);
    }

    @Override
    public Mono<ApplicationStatus> findByName(ApplicationStatusName name) {
        return applicationStatusRepository.findByApplicationStatusName(name);
    }
}
