package com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.itmowork.company_service.adapter.out.kafka.common.RequestType;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ResponseMessage(
        UUID correlationId,
        RequestType eventType,
        boolean ok,
        JsonNode payload,
        JsonNode errorPayload
) {
}
