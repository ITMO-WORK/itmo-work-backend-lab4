package org.ilestegor.applicationservice.adapter.output.kafka.event.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record EventMessage(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        JsonNode payload
) { }
