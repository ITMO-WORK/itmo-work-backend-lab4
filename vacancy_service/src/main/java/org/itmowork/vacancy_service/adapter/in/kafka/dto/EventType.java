package org.itmowork.vacancy_service.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EventType {
    @JsonEnumDefaultValue
    UNKNOWN,
    VACANCY_EXISTS,
    VACANCY_IS_PUBLISHED,
    VACANCY_TITLE,
    VACANCY_COMPANY_ID
}
