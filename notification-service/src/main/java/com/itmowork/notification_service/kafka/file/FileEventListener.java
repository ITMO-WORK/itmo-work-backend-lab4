package com.itmowork.notification_service.kafka.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.notification_service.dto.event.EventMessage;
import com.itmowork.notification_service.dto.event.EventType;
import com.itmowork.notification_service.dto.event.file.ResumeUploadEvent;
import com.itmowork.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;


    @KafkaListener(
            topics = "files.events",
            groupId = "notification.service"
    )
    public void listen(EventMessage message) {

        log.info("Received event: type={}, id={}",
                message.eventType(), message.eventId());

        if (message.eventType() == EventType.RESUME_UPLOAD_EVENT) {

            ResumeUploadEvent payload = parsePayload(message, ResumeUploadEvent.class);

            notificationService.notifyResumeUploaded(payload);
        }
    }

    private  <T> T parsePayload(EventMessage message, Class<T> type) {
        try {
            return objectMapper.treeToValue(message.payload(), type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot parse payload", e);
        }
    }
}
