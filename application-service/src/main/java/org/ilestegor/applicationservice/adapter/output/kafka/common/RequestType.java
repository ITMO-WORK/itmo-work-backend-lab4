package org.ilestegor.applicationservice.adapter.output.kafka.common;

import lombok.Getter;

@Getter
public enum RequestType {
    VACANCY_TITLE("vacancy_title"),
    VACANCY_EXISTS("vacancy_exists"),
    VACANCY_IS_PUBLISHED("vacancy_is_published"),
    VACANCY_COMPANY_ID("vacancy_company_id");

    private final String value;

    RequestType(String value) {
        this.value = value;
    }
}
