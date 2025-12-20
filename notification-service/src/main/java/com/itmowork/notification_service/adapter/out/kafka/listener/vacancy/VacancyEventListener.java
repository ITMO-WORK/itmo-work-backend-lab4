package com.itmowork.notification_service.adapter.out.kafka.listener.vacancy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.EventMessage;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.EventType;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.vacancy.VacancyStatusChangeEvent;
import com.itmowork.notification_service.application.usecase.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyEventListener {

    private final NotificationPort notificationPort;
    private final ObjectMapper objectMapper;


    @KafkaListener(
            topics = "vacancy.events",
            groupId = "notification.service"
    )
    public void listen(EventMessage message) {

        log.info("Received event: type={}, id={}",
                message.eventType(), message.eventId());

        if (message.eventType() == EventType.VACANCY_STATUS_CHANGE) {

            VacancyStatusChangeEvent payload = parsePayload(message, VacancyStatusChangeEvent.class);

            notificationPort.notifyVacancyStatusUpdated(payload);
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
