package org.itmo.work.fileservice.infrastructure.file.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.infrastructure.common.producer.KafkaProducer;
import org.itmo.work.fileservice.infrastructure.config.KafkaProps;
import org.itmo.work.fileservice.infrastructure.dto.EventMessage;
import org.itmo.work.fileservice.infrastructure.dto.EventType;
import org.itmo.work.fileservice.infrastructure.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.infrastructure.file.FileEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaFileEventPublisher implements FileEventPublisher {

    private final KafkaProducer kafkaProducer;
    private final KafkaProps kafkaProps;
    private final ObjectMapper objectMapper;

    @Override
    public void publishFileUploadedEvent(FileUploadEvent fileUploadEvent) {
        EventMessage message = EventMessage.builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.RESUME_UPLOAD_EVENT)
                .occurredAt(Instant.now())
                .payload(objectMapper.valueToTree(fileUploadEvent))
                .build();
        kafkaProducer.send(
                kafkaProps.topics().filesEvents(),
                fileUploadEvent.fileId().toString(),
                message

        );
    }
}
