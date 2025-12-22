package com.itmowork.company_service.adapter.out.kafka.user.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserRequestPayLoad(
        String ownerFullName,
        String ownerEmail,
        String ownerPassword
) {
}
