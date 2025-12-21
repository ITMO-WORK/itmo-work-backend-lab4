package org.itmowork.vacancy_service.application.port.in;

import java.util.UUID;

public interface VacancyQueriesUseCase {
    UUID getCompanyId(UUID vacancyId);
    String getTitle(UUID vacancyId);
    boolean isPublished(UUID vacancyId);
    boolean exists(UUID vacancyId);
}