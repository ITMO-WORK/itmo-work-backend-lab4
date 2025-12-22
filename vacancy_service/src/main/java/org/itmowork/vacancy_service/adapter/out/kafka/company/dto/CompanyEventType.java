package org.itmowork.vacancy_service.adapter.out.kafka.company.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum CompanyEventType {
    @JsonEnumDefaultValue
    UNKNOWN,
    COMPANY_EXISTS,
    COMPANY_VALIDATE_OWNERSHIP
}