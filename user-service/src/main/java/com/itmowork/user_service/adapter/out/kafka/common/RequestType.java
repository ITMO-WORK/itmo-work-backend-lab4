package com.itmowork.user_service.adapter.out.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestType {

    USER_CREATE_EVENT("user_create_event");

    private final String name;
}
