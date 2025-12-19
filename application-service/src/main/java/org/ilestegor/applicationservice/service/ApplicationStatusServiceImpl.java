package org.ilestegor.applicationservice.dirty.service;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.dirty.model.ApplicationStatus;
import org.ilestegor.applicationservice.dirty.model.ApplicationStatusName;
import org.ilestegor.applicationservice.dirty.repository.ApplicationStatusRepository;
import org.ilestegor.applicationservice.dirty.service.interfaces.ApplicationStatusService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ApplicationStatusServiceImpl implements ApplicationStatusService {
    private final ApplicationStatusRepository applicationStatusRepository;


    @Override
    public Mono<ApplicationStatus> findApplicationStatusByApplicationStatusId(Long applicationStatusId) {
        return applicationStatusRepository.findApplicationStatusById(applicationStatusId);
    }

    @Override
    public Mono<ApplicationStatus> findApplicationStatusByApplicationStatusName(ApplicationStatusName name) {
        return applicationStatusRepository.findByApplicationStatusName(name);
    }
}
