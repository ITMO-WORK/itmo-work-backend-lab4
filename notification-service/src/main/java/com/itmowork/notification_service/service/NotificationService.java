package com.itmowork.notification_service.service;

import com.itmowork.notification_service.dto.event.application.ApplicationStatusUpdateEvent;
import com.itmowork.notification_service.dto.notification.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;


    public void notifyApplicationStatusUpdated(ApplicationStatusUpdateEvent event) {

        String message = String.format(
                "Ваш отклик на вакансию \"%s\" изменён: %s → %s",
                event.vacancyTitle(),
                event.oldStatus(),
                event.newStatus()
        );

        NotificationDto notification = new NotificationDto(
                "Изменение статуса отклика",
                message,
                Instant.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/notifications/user/" + event.userId(),
                notification
        );
    }
}
