package org.itmo.work.fileservice.dto.request;

import java.util.UUID;

public record UploadRequest(
        UUID applicationId,
        UUID userId
) {
}
