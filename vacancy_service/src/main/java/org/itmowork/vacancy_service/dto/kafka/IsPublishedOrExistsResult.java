package org.itmowork.vacancy_service.dto.kafka;

import java.util.UUID;

public record IsPublishedOrExistsResult(UUID vacancyId, boolean published) {}