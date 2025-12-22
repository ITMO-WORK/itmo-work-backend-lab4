package org.ilestegor.applicationservice.adapter.input.kafka.common;

public record ErrorPayload(
        ErrorCode code,
        String message
) {
}
