package org.ilestegor.applicationservice.adapter.output.kafka.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    BAD_REQUEST("bad_request"),
    UNSUPPORTED_OPERATION("unsupported_operation"),
    INTERNAL_ERROR("internal_error"),
    FORBIDDEN("forbidden"),
    UNAUTHORIZED("unauthorized");

    private final String value;
}
