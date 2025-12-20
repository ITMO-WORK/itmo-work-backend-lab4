package org.ilestegor.applicationservice.adapter.input.web.dto.response;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationCreateResponseDto(
        UUID id,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String coverLetter
) {
}
