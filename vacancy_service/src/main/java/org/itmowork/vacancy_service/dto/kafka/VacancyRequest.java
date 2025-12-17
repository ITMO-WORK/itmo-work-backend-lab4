package org.itmowork.vacancy_service.dto.kafka;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record VacancyRequest(
        String type,
        UUID correlationId,
        JsonNode payload
) {}