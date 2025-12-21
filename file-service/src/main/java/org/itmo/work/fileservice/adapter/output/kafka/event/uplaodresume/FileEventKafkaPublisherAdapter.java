package org.itmo.work.fileservice.adapter.output.kafka.event.uplaodresume;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.adapter.output.kafka.common.KafkaProducer;
import org.itmo.work.fileservice.adapter.output.kafka.config.KafkaProps;
import org.itmo.work.fileservice.adapter.output.kafka.dto.EventMessage;
import org.itmo.work.fileservice.adapter.output.kafka.dto.EventType;
import org.itmo.work.fileservice.adapter.output.kafka.dto.events.FileUploadEvent;
import org.itmo.work.fileservice.application.port.output.FileEventPublisherPort;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileEventKafkaPublisherAdapter implements FileEventPublisherPort {
    private final KafkaProducer kafkaProducer;
    private final KafkaProps kafkaProps;
    private final ObjectMapper objectMapper;

    @Override
    public void publishFileUploadedEvent(FileUploadEvent event) {
        publishEvent(
                kafkaProps.topics().filesEvents(),
                event.fileId().toString(),
                EventType.RESUME_UPLOAD_EVENT,
                event
        );
    }


    private void publishEvent(String topic, String key, EventType eventType, Object payload) {
        EventMessage message = EventMessage.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .payload(objectMapper.valueToTree(payload))
                .build();

        kafkaProducer.send(topic, key, message);
    }
}
