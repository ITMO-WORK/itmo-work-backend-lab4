package org.ilestegor.applicationservice.dirty.service.interfaces;

import org.ilestegor.applicationservice.dirty.model.ApplicationStatus;
import org.ilestegor.applicationservice.dirty.model.ApplicationStatusName;
import reactor.core.publisher.Mono;

public interface ApplicationStatusService {
    Mono<ApplicationStatus> findApplicationStatusByApplicationStatusId(Long applicationStatusId);

    Mono<ApplicationStatus> findApplicationStatusByApplicationStatusName(ApplicationStatusName name);


}
