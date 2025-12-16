package org.itmowork.vacancy_service.dto.kafka;

public record ErrorPayload(
        String code,
        String message
) {}