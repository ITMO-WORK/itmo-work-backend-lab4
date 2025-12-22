package org.ilestegor.applicationservice.adapter.output.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestType {
    VACANCY_TITLE("vacancy_title"),
    VACANCY_EXISTS("vacancy_exists"),
    VACANCY_IS_PUBLISHED("vacancy_is_published"),
    VACANCY_COMPANY_ID("vacancy_company_id"),
    COMPANY_VALIDATE_OWNERSHIP("company_validate_ownership"),
    USER_EXISTS_EVENT("user_exists_event");

    private final String value;
}
