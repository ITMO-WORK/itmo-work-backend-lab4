package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.request;

import java.util.UUID;

public record VacancyIdPayload (
        UUID vacancyId
)
{}
