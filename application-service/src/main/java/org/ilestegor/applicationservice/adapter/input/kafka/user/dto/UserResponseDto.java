package org.ilestegor.applicationservice.adapter.input.kafka.user.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserResponseDto(
        UUID userId,
        String ownerFullName,
        String ownerEmail
) {
}