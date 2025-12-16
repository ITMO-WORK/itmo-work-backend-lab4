package org.itmowork.vacancy_service.dto.kafka;

import java.util.UUID;

public record IsPublishedResult(UUID vacancyId, boolean published) {}