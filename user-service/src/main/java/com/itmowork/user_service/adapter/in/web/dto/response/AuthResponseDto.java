package com.itmowork.user_service.adapter.in.web.dto.response;

import java.util.UUID;

public record AuthResponseDto(
        UUID id,
        String token
) {}
