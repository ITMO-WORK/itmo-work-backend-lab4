package com.itmowork.user_service.application.dto.result;

import java.util.UUID;

public record AuthResult(
        UUID id,
        String token
) {}