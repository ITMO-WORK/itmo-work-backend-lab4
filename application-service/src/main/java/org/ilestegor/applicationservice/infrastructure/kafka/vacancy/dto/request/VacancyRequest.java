package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.VacancyOperations;

import java.util.UUID;

@Builder(toBuilder = true)
public record VacancyRequest(
        UUID correlationId,
        VacancyOperations type,
        JsonNode payload
){}
