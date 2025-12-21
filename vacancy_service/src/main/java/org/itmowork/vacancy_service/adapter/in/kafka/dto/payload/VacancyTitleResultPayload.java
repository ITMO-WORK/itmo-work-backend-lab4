package org.itmowork.vacancy_service.adapter.in.kafka.dto.payload;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VacancyTitleResultPayload(UUID vacancyId, String title) {}