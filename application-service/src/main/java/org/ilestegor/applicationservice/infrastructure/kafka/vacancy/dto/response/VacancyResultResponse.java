package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response;

import org.apache.kafka.common.protocol.types.Field;

import java.util.UUID;

public record VacancyResultResponse(UUID vacancyId, Boolean result) {
}
