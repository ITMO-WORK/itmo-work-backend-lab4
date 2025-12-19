package org.ilestegor.applicationservice.dirty.infrastructure.kafka.application;

import org.ilestegor.applicationservice.dirty.infrastructure.kafka.dto.events.ApplicationStatusChangeEvent;
import reactor.core.publisher.Mono;

public interface ApplicationEventPublisher {
    Mono<Void> publishStatusChanged(ApplicationStatusChangeEvent applicationStatusChangeEvent);
}
