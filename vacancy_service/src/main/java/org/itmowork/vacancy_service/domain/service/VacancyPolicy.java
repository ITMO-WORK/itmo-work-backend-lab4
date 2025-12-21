package org.itmowork.vacancy_service.domain.service;

import org.itmowork.vacancy_service.domain.exception.exceptions.InvalidVacancySalaryException;
import org.itmowork.vacancy_service.domain.exception.exceptions.InvalidVacancyStatusChangeException;
import org.itmowork.vacancy_service.domain.exception.exceptions.InvalidVacancyStatusException;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;

import java.util.Map;
import java.util.Set;

public final class VacancyPolicy {

    private VacancyPolicy() {}

    private static final Map<VacancyStatusName, Set<VacancyStatusName>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            VacancyStatusName.DRAFT, Set.of(VacancyStatusName.PUBLISHED, VacancyStatusName.CLOSED),
            VacancyStatusName.PUBLISHED, Set.of(VacancyStatusName.DRAFT, VacancyStatusName.CLOSED)
    );

    public static void validateSalaryBounds(Integer salaryFrom, Integer salaryTo) {
        if (salaryFrom != null && salaryFrom < 0) {
            throw new InvalidVacancySalaryException("salary_from must be non-negative");
        }
        if (salaryTo != null && salaryTo < 0) {
            throw new InvalidVacancySalaryException("salary_to must be non-negative");
        }
        if (salaryFrom != null && salaryTo != null && salaryFrom > salaryTo) {
            throw new InvalidVacancySalaryException("salary_from cannot be greater than salary_to");
        }
    }

    public static void assertUpdateAllowed(VacancyStatusName currentStatus) {
        if (currentStatus != VacancyStatusName.DRAFT && currentStatus != VacancyStatusName.PUBLISHED) {
            throw new InvalidVacancyStatusException("Update allowed only for DRAFT or PUBLISHED vacancies");
        }
    }

    public static void assertTransitionAllowed(VacancyStatusName currentStatus, VacancyStatusName newStatus) {
        Set<VacancyStatusName> allowed = ALLOWED_STATUS_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidVacancyStatusChangeException(
                    "Impossible to change status from " + currentStatus + " to " + newStatus
            );
        }
    }
}
