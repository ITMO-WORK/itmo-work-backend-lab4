package org.itmo.work.fileservice.application.usecase.updateresume;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.application.port.input.UpdateResumePort;
import org.itmo.work.fileservice.application.port.output.*;
import org.itmo.work.fileservice.application.usecase.updateresume.dto.UpdateResumeResponse;
import org.itmo.work.fileservice.domain.exception.ApplicationNotFoundException;
import org.itmo.work.fileservice.domain.exception.ResumeNotFoundException;
import org.itmo.work.fileservice.domain.model.ApplicationRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateResumeUseCase implements UpdateResumePort {

    private final FileRepositoryPort storedFileRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final FileStoragePropsPort fileStoragePropsPort;
    private final ApplicationRegistryPort applicationRegistryPort;

    @Override
    public UpdateResumeResponse updateResume(UUID applicationId, MultipartFile file) {

        boolean exists = applicationRegistryPort.exists(applicationId);
        if (!exists) throw new ApplicationNotFoundException();


        var resume = storedFileRepositoryPort.getResumeByApplicationId(applicationId).orElseThrow(ApplicationNotFoundException::new);

        UUID fileId = resume.getId();

        if (fileId == null)
            throw new ResumeNotFoundException();

        String bucket = fileStoragePropsPort.bucket();
        String objectKey = resume.getObjectKey();

        fileStoragePort.put(bucket, objectKey, file);

        String fileName = safeName(file.getOriginalFilename());
        resume.setOriginalFileName(fileName);
        resume.setContentType(file.getContentType());
        resume.setSizeBytes(file.getSize());
        storedFileRepositoryPort.save(resume);

        return new UpdateResumeResponse(fileId, fileName);

    }

    private String safeName(String original) {
        if (original == null || original.isBlank()) return "resume";
        return original;
    }
}
