package com.itmowork.user_service.adapter.in.kafka.dto.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserExistsResultPayload(
        UUID userId,
        String ownerFullName,
        String ownerEmail
) {}
