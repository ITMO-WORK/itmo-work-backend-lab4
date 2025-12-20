package org.ilestegor.applicationservice.adapter.output.persistence;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.application.port.output.ApplicationRepositoryPort;
import org.ilestegor.applicationservice.domain.Application;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationR2DBCRepository implements ApplicationRepositoryPort {

    private final ApplicationRepository applicationRepository;

    @Override
    public Mono<Boolean> existsByUserIdAndVacancyId(UUID userId, UUID vacancyId) {
        return applicationRepository.existsByUserIdAndVacancyId(userId, vacancyId);
    }

    @Override
    public Mono<Application> findApplicationsByUserIdAndVacancyId(UUID userId, UUID vacancyId) {
        return applicationRepository.findApplicationsByUserIdAndVacancyId(userId, vacancyId);
    }

    @Override
    public Mono<Application> findById(UUID applicationId) {
        return applicationRepository.findById(applicationId);
    }

    @Override
    public Flux<Application> findAllByVacancyId(UUID vacancyId, Pageable pageable) {
        return applicationRepository.findAllByVacancyId(vacancyId, pageable);
    }

    @Override
    public Mono<Long> countApplicationByVacancyId(UUID vacancyId) {
        return applicationRepository.countApplicationByVacancyId(vacancyId);
    }

    @Override
    public Mono<UUID> findVacancyIdById(UUID applicationId) {
        return applicationRepository.findVacancyIdById(applicationId);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return applicationRepository.existsById(id);
    }

    @Override
    public Mono<Integer> updateFileId(UUID id, UUID fileId) {
        return applicationRepository.updateFileId(id, fileId);
    }

    @Override
    public Mono<Application> save(Application application) {
        return applicationRepository.save(application);
    }
}
