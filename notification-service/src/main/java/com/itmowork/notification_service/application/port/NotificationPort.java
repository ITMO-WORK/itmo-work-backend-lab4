package com.itmowork.notification_service.application.port;

import com.itmowork.notification_service.adapter.out.kafka.dto.event.application.ApplicationStatusUpdateEvent;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.file.ResumeUploadEvent;
import com.itmowork.notification_service.adapter.out.kafka.dto.event.vacancy.VacancyStatusChangeEvent;

public interface NotificationPort {

    void notifyApplicationStatusUpdated(ApplicationStatusUpdateEvent event);
    void notifyVacancyStatusUpdated(VacancyStatusChangeEvent event);
    void notifyResumeUploaded(ResumeUploadEvent event);
}
