package com.itmowork.notification_service.dto.notification;

import java.time.Instant;

public record NotificationDto(
        String title,
        String message,
        Instant createdAt
) {
}
