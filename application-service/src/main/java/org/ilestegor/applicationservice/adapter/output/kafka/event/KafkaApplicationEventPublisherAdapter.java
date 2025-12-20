package org.ilestegor.applicationservice.adapter.output.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.output.kafka.common.KafkaProducer;
import org.ilestegor.applicationservice.adapter.output.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventType;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationStatusChangeEvent;
import org.ilestegor.applicationservice.application.port.output.ApplicationEventPublisherPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaApplicationEventPublisherAdapter implements ApplicationEventPublisherPort {
    private final ObjectMapper objectMapper;
    private final KafkaProps kafkaProps;
    private final KafkaProducer kafkaProducer;

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
