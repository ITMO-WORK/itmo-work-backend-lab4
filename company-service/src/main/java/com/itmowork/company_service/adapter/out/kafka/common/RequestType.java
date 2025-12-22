package com.itmowork.company_service.adapter.out.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestType {

    USER_CREATE_EVENT("user_create_event"),
    USER_CREATE_RESPONSE_EVENT("user_create_response_event");

    private final String name;
}
