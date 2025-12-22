package com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto;

import java.util.UUID;

public record UserResponsePayLoad(
        UUID id,
        String token
) {
}
