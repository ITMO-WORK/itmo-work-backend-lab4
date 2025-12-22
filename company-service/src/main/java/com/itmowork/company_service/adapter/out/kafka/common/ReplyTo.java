package com.itmowork.company_service.adapter.out.kafka.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReplyTo {
    COMPANY_RESPONSE("company.response"),
    APPLICATION_RESPONSE("application.response"),
    VACANCY_RESPONSE("vacancy.response");

    private final String value;

    @JsonValue
    public String json() {
        return value;
    }

    @JsonCreator
    public static ReplyTo from(String value) {
        if (value == null) return null;
        for (ReplyTo r : values()) {
            if (r.value.equalsIgnoreCase(value)) return r;
        }
        return null;
    }
}