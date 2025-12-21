package org.ilestegor.applicationservice.adapter.output.kafka.vacancy.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VacancyIdPayload(
        UUID vacancyId
) {
}
