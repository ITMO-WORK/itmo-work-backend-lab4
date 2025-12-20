package com.itmowork.notification_service.adapter.out.kafka.dto.notification;

import java.time.Instant;

public record NotificationDto(
        String title,
        String message,
        Instant createdAt
) {
}
