package org.ilestegor.applicationservice.adapter.output.feign.user.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String fullName,
        String email
) {
}