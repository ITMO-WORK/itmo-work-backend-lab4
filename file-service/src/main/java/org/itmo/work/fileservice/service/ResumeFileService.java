package org.itmo.work.fileservice.service;

import org.itmo.work.fileservice.dto.response.UploadResumeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ResumeFileService {

    UploadResumeResponse uploadResume(MultipartFile file, UUID replacedField);
}
