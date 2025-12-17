package org.ilestegor.applicationservice.infrastructure.kafka.application;

import org.ilestegor.applicationservice.infrastructure.kafka.dto.events.ApplicationStatusChangeEvent;
import reactor.core.publisher.Mono;

public interface ApplicationEventPublisher {
    Mono<Void> publishStatusChanged(ApplicationStatusChangeEvent applicationStatusChangeEvent);
}
