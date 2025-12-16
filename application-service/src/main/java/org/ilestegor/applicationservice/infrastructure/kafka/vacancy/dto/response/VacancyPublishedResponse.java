package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response;

import java.util.UUID;

public record VacancyPublishedResponse (Boolean published, UUID vacancyId) {
}
