package org.ilestegor.applicationservice.dirty.infrastructure.kafka.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.dirty.infrastructure.kafka.application.ApplicationEventPublisher;
import org.ilestegor.applicationservice.dirty.infrastructure.kafka.common.producer.KafkaProducer;
import org.ilestegor.applicationservice.dirty.infrastructure.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.dirty.infrastructure.kafka.dto.EventMessage;
import org.ilestegor.applicationservice.dirty.infrastructure.kafka.dto.EventType;
import org.ilestegor.applicationservice.dirty.infrastructure.kafka.dto.events.ApplicationStatusChangeEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaApplicationEventPublisher implements ApplicationEventPublisher {

    private final KafkaProducer kafkaProducer;
    private final KafkaProps kafkaProps;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publishStatusChanged(ApplicationStatusChangeEvent applicationStatusChangeEvent) {
        EventMessage eventMessage = EventMessage.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.APPLICATION_STATUS_CHANGE)
                .occurredAt(Instant.now())
                .payload(objectMapper.valueToTree(applicationStatusChangeEvent)).build();

        return kafkaProducer.send(
                kafkaProps.topics().applicationsEvents(),
                applicationStatusChangeEvent.applicationId().toString(),
                eventMessage
        );
    }
}
