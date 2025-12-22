package org.ilestegor.applicationservice.adapter.input.kafka.vacancy.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VacancyCompanyIdPayload(
        UUID vacancyId,
        UUID companyId
) {
}
