package com.itmowork.notification_service;

import com.itmowork.notification_service.dto.event.application.ApplicationStatusUpdateEvent;
import com.itmowork.notification_service.dto.event.file.ResumeUploadEvent;
import com.itmowork.notification_service.dto.event.vacancy.VacancyStatusChangeEvent;
import com.itmowork.notification_service.dto.notification.NotificationDto;
import com.itmowork.notification_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldSendApplicationStatusUpdateNotification() {
        ApplicationStatusUpdateEvent event = new ApplicationStatusUpdateEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Backend Developer",
                "NEW",
                "ACCEPTED"
        );

        notificationService.notifyApplicationStatusUpdated(event);

        ArgumentCaptor<NotificationDto> captor =
                ArgumentCaptor.forClass(NotificationDto.class);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/user/" + event.userId()),
                captor.capture()
        );

        NotificationDto dto = captor.getValue();
        assertThat(dto.title()).isEqualTo("Изменение статуса отклика");
        assertThat(dto.message()).contains("Backend Developer");
        assertThat(dto.message()).contains("NEW → ACCEPTED");
    }

    @Test
    void shouldSendVacancyStatusUpdateNotification() {
        VacancyStatusChangeEvent event = new VacancyStatusChangeEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "DRAFT",
                "PUBLISHED"
        );

        notificationService.notifyVacancyStatusUpdated(event);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/vacancy/" + event.vacancyId()),
                any(NotificationDto.class)
        );
    }

    @Test
    void shouldSendResumeUploadedNotification() {
        ResumeUploadEvent event = new ResumeUploadEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "cv_backend.pdf",
                "application/pdf",
                Instant.now()
        );

        notificationService.notifyResumeUploaded(event);

        ArgumentCaptor<NotificationDto> captor =
                ArgumentCaptor.forClass(NotificationDto.class);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/notifications/resume/" + event.userId()),
                captor.capture()
        );

        NotificationDto dto = captor.getValue();

        assertThat(dto.title())
                .isEqualTo("Загрузка резюме");

        assertThat(dto.message())
                .contains("cv_backend.pdf")
                .contains("успешно загружено");

        assertThat(dto.createdAt())
                .isNotNull();
    }

}