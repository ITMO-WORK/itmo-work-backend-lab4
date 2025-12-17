package org.itmowork.vacancy_service.dto.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record VacancyResponse(
        VacancyOperations vacancyOperations,
        UUID correlationId,
        boolean ok,
        JsonNode payload,
        ErrorPayload errorPayload
) {}