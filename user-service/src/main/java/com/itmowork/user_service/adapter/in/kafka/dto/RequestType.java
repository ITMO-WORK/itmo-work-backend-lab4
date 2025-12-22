package com.itmowork.user_service.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestType {

    @JsonEnumDefaultValue
    UNKNOWN("unknown"),
    USER_CREATE_EVENT("user_create_event"),
    USER_EXISTS_EVENT("user_exists_event");

    private final String name;
}