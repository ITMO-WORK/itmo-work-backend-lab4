package com.itmowork.user_service.application.dto.result;

import java.util.UUID;

public record UserResult(
        UUID id,
        String fullName,
        String email
) {}
