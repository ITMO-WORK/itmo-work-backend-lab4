package org.ilestegor.applicationservice.adapter.output.kafka.vacancy.dto;

import java.util.UUID;

public record VacancyIdPayload(
        UUID vacancyId
) {
}
