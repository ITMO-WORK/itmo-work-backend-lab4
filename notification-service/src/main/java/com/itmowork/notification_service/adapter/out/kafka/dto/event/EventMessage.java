package com.itmowork.notification_service.adapter.out.kafka.dto.event;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventMessage(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        JsonNode payload
) {
}
