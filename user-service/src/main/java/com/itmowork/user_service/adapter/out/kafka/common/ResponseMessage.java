package com.itmowork.user_service.adapter.out.kafka.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ResponseMessage(
        UUID correlationId,
        RequestType eventType,
        boolean ok,
        JsonNode payload,
        ErrorPayload errorPayload
) {
}
