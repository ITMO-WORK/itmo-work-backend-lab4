package org.ilestegor.applicationservice.adapter.output.kafka.common;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReplyTo {
    APPLICATION_RESPONSE("application.response");

    @JsonValue
    private final String value;
}
