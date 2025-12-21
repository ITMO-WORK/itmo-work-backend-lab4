package org.ilestegor.applicationservice.adapter.output.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ReplyTo {
    APPLICATION_RESPONSE("application.response");

    private final String value;
}
