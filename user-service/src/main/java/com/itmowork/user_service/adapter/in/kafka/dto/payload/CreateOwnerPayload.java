package com.itmowork.user_service.adapter.in.kafka.dto.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateOwnerPayload(
        String ownerFullName,
        String ownerEmail,
        String ownerPassword
) {}