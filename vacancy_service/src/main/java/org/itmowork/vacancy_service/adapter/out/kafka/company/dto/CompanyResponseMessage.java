package org.itmowork.vacancy_service.adapter.out.kafka.company.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.itmowork.vacancy_service.adapter.in.kafka.dto.ErrorPayload;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CompanyResponseMessage(
        CompanyEventType eventType,
        UUID correlationId,
        boolean ok,
        JsonNode payload,
        ErrorPayload errorPayload
) {}