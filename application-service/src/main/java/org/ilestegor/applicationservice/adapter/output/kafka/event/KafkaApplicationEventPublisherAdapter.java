package org.ilestegor.applicationservice.adapter.output.kafka.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.output.kafka.common.SpringKafkaProducer;
import org.ilestegor.applicationservice.adapter.output.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventType;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.events.ApplicationCreateEventDto;
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
    private final SpringKafkaProducer springKafkaProducer;

    @Override
    public Mono<Void> publishStatusChanged(ApplicationStatusChangeEvent applicationStatusChangeEvent) {
        return publish(
                kafkaProps.topics().applicationsEvents(),
                EventType.APPLICATION_STATUS_CHANGE,
                applicationStatusChangeEvent.applicationId().toString(),
                applicationStatusChangeEvent
        );
    }

    @Override
    public Mono<Void> publishApplicationCreate(ApplicationCreateEventDto applicationCreateEventDto) {
        return publish(
                kafkaProps.topics().applicationsEvents(),
                EventType.APPLICATION_CREATE,
                applicationCreateEventDto.applicationId().toString(),
                applicationCreateEventDto
        );
    }

    private Mono<Void> publish(String topic, EventType type, String key, Object payload) {
        EventMessage msg = EventMessage.builder()
                .eventId(UUID.randomUUID())
                .eventType(type)
                .occurredAt(Instant.now())
                .payload(objectMapper.valueToTree(payload))
                .build();

        return springKafkaProducer.send(topic, key, msg);
    }
}
