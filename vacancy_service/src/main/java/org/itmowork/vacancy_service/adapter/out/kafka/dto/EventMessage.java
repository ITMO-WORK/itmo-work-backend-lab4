package org.itmowork.vacancy_service.adapter.out.kafka.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record EventMessage(
        UUID eventId,
        EventType eventType,
        Instant occurredAt,
        JsonNode payload
) { }