package org.ilestegor.applicationservice.adapter.input.web.dto.response;

import java.time.LocalDateTime;

public record ApplicationStatusUpdateResponseDto(
        String status,
        LocalDateTime updateAt
) {
}
