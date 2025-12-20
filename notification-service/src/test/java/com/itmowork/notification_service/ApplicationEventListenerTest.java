package com.itmowork.notification_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itmowork.notification_service.dto.event.EventMessage;
import com.itmowork.notification_service.dto.event.EventType;
import com.itmowork.notification_service.dto.event.application.ApplicationStatusUpdateEvent;
import com.itmowork.notification_service.kafka.application.ApplicationEventListener;
import com.itmowork.notification_service.service.NotificationService;
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
class ApplicationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ApplicationEventListener listener;

    @Test
    void shouldThrowIllegalStateExceptionWhenPayloadCannotBeParsed() throws Exception {
        ObjectNode invalidPayload = new ObjectMapper().createObjectNode();

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.APPLICATION_STATUS_CHANGE,
                Instant.now(),
                invalidPayload
        );

        when(objectMapper.treeToValue(invalidPayload, ApplicationStatusUpdateEvent.class))
                .thenThrow(new JsonProcessingException("boom") {});

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listener.listen(message)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Cannot parse payload");

        verify(notificationService, never())
                .notifyApplicationStatusUpdated(any());
    }

    @Test
    void shouldHandleApplicationStatusChangeEvent() throws Exception {

        ApplicationStatusUpdateEvent payload = new ApplicationStatusUpdateEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Backend Developer",
                "NEW",
                "ACCEPTED"
        );

        ObjectNode payloadNode = new ObjectMapper().valueToTree(payload);

        when(objectMapper.treeToValue(payloadNode, ApplicationStatusUpdateEvent.class))
                .thenReturn(payload);

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.APPLICATION_STATUS_CHANGE,
                Instant.now(),
                payloadNode
        );

        listener.listen(message);

        verify(notificationService)
                .notifyApplicationStatusUpdated(payload);
    }

    @Test
    void shouldIgnoreUnknownEventType() {
        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.VACANCY_STATUS_CHANGE,
                Instant.now(),
                new ObjectMapper().createObjectNode()
        );

        listener.listen(message);

        verify(notificationService, never())
                .notifyApplicationStatusUpdated(any());
    }
}