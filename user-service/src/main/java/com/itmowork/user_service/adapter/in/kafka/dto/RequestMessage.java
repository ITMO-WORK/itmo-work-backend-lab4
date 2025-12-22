package com.itmowork.user_service.adapter.in.kafka.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RequestMessage(
        RequestType eventType,
        UUID correlationId,
        ReplyTo replyTo,
        JsonNode payload
) {}
