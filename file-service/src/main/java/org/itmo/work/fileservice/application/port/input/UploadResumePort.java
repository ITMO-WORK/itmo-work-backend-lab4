package org.itmo.work.fileservice.application.port.input;


import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeRequest;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UploadResumePort {
    UploadResumeResponse uploadResume(MultipartFile file, UUID replacedField, UploadResumeRequest uploadRequest);
}
