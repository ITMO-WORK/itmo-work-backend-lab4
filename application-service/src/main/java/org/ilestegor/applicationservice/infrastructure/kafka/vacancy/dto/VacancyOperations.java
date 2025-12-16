package org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto;

import lombok.Getter;

@Getter
public enum VacancyOperations {
    VACANCY_IS_PUBLISHED("vacancy_is_published"),
    VACANCY_IS_EXISTS("vacancy_is_exists")
    ;

    private final String value;
    VacancyOperations(String value) {
        this.value = value;
    }

}
