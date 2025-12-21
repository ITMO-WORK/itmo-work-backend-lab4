package org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto;

import java.util.UUID;

public record VacancyResultPayload(
        UUID vacancyId,
        boolean result
) {
}
