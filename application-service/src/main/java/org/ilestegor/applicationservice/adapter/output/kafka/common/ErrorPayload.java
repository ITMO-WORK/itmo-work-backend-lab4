package org.ilestegor.applicationservice.adapter.output.kafka.common;

public record ErrorPayload(
        ErrorCode code,
        String message
) {
}
