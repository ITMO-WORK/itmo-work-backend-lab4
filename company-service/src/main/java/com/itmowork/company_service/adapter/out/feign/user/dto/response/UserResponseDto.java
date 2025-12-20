package com.itmowork.company_service.adapter.out.feign.user.dto.response;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String token
) {
}
