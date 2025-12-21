package org.ilestegor.applicationservice.adapter.input.kafka.vacancy.responselistener.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VacancyTitlePayload(
        UUID vacancyId,
        String title
) {
}
