package com.itmowork.notification_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.EventMessage;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.EventType;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.vacancy.VacancyStatusChangeEvent;
import com.itmowork.notification_service.adapter.out.kafka.listener.vacancy.VacancyEventListener;
import com.itmowork.notification_service.application.usecase.NotificationPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyEventListenerTest {

    @Mock
    private NotificationPort notificationUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VacancyEventListener listener;

    @Test
    void shouldThrowIllegalStateExceptionWhenPayloadCannotBeParsed() throws Exception {
        ObjectNode invalidPayload = new ObjectMapper().createObjectNode();

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.VACANCY_STATUS_CHANGE,
                Instant.now(),
                invalidPayload
        );

        when(objectMapper.treeToValue(invalidPayload, VacancyStatusChangeEvent.class))
                .thenThrow(new JsonProcessingException("boom") {});

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listener.listen(message)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Cannot parse payload");

        verify(notificationUseCase, never())
                .notifyVacancyStatusUpdated(any());
    }

    @Test
    void shouldHandleVacancyStatusChangeEvent() throws Exception {

        VacancyStatusChangeEvent payload = new VacancyStatusChangeEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "DRAFT",
                "PUBLISHED"
        );

        ObjectMapper realMapper = new ObjectMapper();
        var payloadNode = realMapper.valueToTree(payload);

        when(objectMapper.treeToValue(payloadNode, VacancyStatusChangeEvent.class))
                .thenReturn(payload);

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.VACANCY_STATUS_CHANGE,
                Instant.now(),
                payloadNode
        );

        listener.listen(message);

        verify(notificationUseCase)
                .notifyVacancyStatusUpdated(payload);
    }

    @Test
    void shouldIgnoreUnknownEventType() {

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.APPLICATION_STATUS_CHANGE,
                Instant.now(),
                new ObjectMapper().createObjectNode()
        );

        listener.listen(message);

        verify(notificationUseCase, never())
                .notifyVacancyStatusUpdated(any());
    }
}