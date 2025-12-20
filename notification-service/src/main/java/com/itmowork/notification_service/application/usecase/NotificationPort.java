package com.itmowork.notification_service.application.usecase;

import com.itmowork.notification_service.adapter.out.kafka.dto.event.application.ApplicationStatusUpdateEvent;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.file.ResumeUploadEvent;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.vacancy.VacancyStatusChangeEvent;
import com.itmowork.notification_service.adapter.out.kafka.dto.notification.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationPort implements com.itmowork.notification_service.application.port.NotificationPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
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

    @Override
    public void notifyVacancyStatusUpdated(VacancyStatusChangeEvent event) {
       String message = String.format(
                "Статус вашей вакансии изменён: %s → %s",
                event.oldStatus(),
                event.newStatus()
       );
         NotificationDto notification = new NotificationDto(
                 "Изменение статуса вакансии",
                 message,
                 Instant.now()
         );


         messagingTemplate.convertAndSend(
                 "/topic/notifications/vacancy/" + event.vacancyId(),
                 notification
         );
    }

    @Override
    public void notifyResumeUploaded(ResumeUploadEvent event) {
        String message = String.format(
                "Ваше резюме %s было успешно загружено",
                event.originalFileName()
        );
        NotificationDto notification = new NotificationDto(
                "Загрузка резюме",
                message,
                Instant.now()
        );


        messagingTemplate.convertAndSend(
                "/topic/notifications/resume/" + event.userId(),
                notification
        );
    }
}
