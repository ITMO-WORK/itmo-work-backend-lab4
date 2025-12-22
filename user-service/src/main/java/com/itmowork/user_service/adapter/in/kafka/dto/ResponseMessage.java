package com.itmowork.user_service.adapter.in.kafka.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ResponseMessage(
        RequestType eventType,
        UUID correlationId,
        boolean ok,
        JsonNode payload,
        ErrorPayload errorPayload
) {}