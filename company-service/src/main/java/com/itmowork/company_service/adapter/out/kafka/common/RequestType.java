package com.itmowork.company_service.adapter.out.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestType {

    UNKNOWN("unknown"),
    USER_CREATE_EVENT("user_create_event"),
    COMPANY_VALIDATE_OWNERSHIP("company_validate_ownership"),
    COMPANY_EXISTS("company_exists");

    private final String name;
}
