package org.ilestegor.applicationservice.application.port.output;

import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationCreateEventDto;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationStatusChangeEvent;
import reactor.core.publisher.Mono;

public interface ApplicationEventPublisherPort {
    Mono<Void> publishStatusChanged(ApplicationStatusChangeEvent applicationStatusChangeEvent);

    Mono<Void> publishApplicationCreate(ApplicationCreateEventDto applicationCreateEventDto);
}
