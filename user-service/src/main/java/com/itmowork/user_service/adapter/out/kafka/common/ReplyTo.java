package com.itmowork.user_service.adapter.out.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReplyTo {
    COMPANY_RESPONSE("company.response");

    private final String value;
}