package org.itmo.work.fileservice.application.usecase.updateresume.dto;

import java.util.UUID;

public record UpdateResumeResponse(
        UUID fileId,
        String fileName
) {
}
