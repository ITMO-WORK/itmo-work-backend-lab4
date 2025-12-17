package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import org.ilestegor.applicationservice.infrastructure.kafka.common.error.ErrorPayload;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.VacancyOperations;

import java.util.UUID;

public record VacancyResponse (
        VacancyOperations vacancyOperations,
        UUID correlationId,
        boolean ok,
        JsonNode payload,
        ErrorPayload errorPayload

){
}
