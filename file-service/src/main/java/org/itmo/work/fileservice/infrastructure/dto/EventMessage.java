package org.itmo.work.fileservice.infrastructure.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record EventMessage(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        JsonNode payload
) {
}
