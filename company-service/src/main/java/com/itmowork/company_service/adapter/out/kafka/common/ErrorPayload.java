package com.itmowork.company_service.adapter.out.kafka.common;

public record ErrorPayload(
        ErrorCode code,
        String message
) {
}
