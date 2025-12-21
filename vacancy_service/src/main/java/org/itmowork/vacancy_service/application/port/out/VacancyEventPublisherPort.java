package org.itmowork.vacancy_service.application.port.out;

import org.itmowork.vacancy_service.application.dto.event.VacancyStatusChangedEvent;

public interface VacancyEventPublisherPort {
    void publishStatusChanged(VacancyStatusChangedEvent event);
}