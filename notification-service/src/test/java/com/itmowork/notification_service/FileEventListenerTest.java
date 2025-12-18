package com.itmowork.notification_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.itmowork.notification_service.dto.event.EventMessage;
import com.itmowork.notification_service.dto.event.EventType;
import com.itmowork.notification_service.dto.event.file.ResumeUploadEvent;
import com.itmowork.notification_service.kafka.file.FileEventListener;
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
class FileEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FileEventListener listener;

    @Test
    void shouldThrowIllegalStateExceptionWhenPayloadCannotBeParsed() throws Exception {
        ObjectNode invalidPayload = new ObjectMapper().createObjectNode();

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.RESUME_UPLOAD_EVENT,
                Instant.now(),
                invalidPayload
        );

        when(objectMapper.treeToValue(invalidPayload, ResumeUploadEvent.class))
                .thenThrow(new JsonProcessingException("boom") {});

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listener.listen(message)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Cannot parse payload");

        verify(notificationService, never())
                .notifyResumeUploaded(any());
    }

    @Test
    void shouldHandleResumeUploadEvent() throws Exception {
        ResumeUploadEvent payload = new ResumeUploadEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "cv.pdf",
                "application/pdf",
                Instant.now()
        );

        ObjectMapper realMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        ObjectNode payloadNode = realMapper.valueToTree(payload);

        when(objectMapper.treeToValue(payloadNode, ResumeUploadEvent.class))
                .thenReturn(payload);

        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.RESUME_UPLOAD_EVENT,
                Instant.now(),
                payloadNode
        );

        listener.listen(message);

        verify(notificationService)
                .notifyResumeUploaded(payload);
    }

    @Test
    void shouldIgnoreUnknownEventType() {
        EventMessage message = new EventMessage(
                UUID.randomUUID(),
                EventType.APPLICATION_STATUS_CHANGE, // любой другой тип
                Instant.now(),
                new ObjectMapper().createObjectNode()
        );

        listener.listen(message);

        verify(notificationService, never())
                .notifyResumeUploaded(any());
    }


}
