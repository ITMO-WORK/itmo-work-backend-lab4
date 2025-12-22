package org.itmo.work.fileservice.application.port.input;

import org.itmo.work.fileservice.application.usecase.updateresume.dto.UpdateResumeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UpdateResumePort {
    UpdateResumeResponse updateResume(UUID applicationId, MultipartFile file);
}
