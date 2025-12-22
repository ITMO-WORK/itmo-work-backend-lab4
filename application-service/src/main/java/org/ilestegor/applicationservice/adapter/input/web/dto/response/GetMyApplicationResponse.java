package org.ilestegor.applicationservice.adapter.input.web.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record GetMyApplicationResponse(
        UUID id,
        String coverLetter,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String vacancyTitle,
        String userFullName,
        UUID fileId,
        String applicationStatus
) {
}
