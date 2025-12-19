package org.ilestegor.applicationservice.dirty.infrastructure.feign.user.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String fullName,
        String email
) {
}