package org.itmo.work.fileservice.controller;

import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.entity.mime.MultipartPart;
import org.itmo.work.fileservice.dto.response.UploadResumeResponse;
import org.itmo.work.fileservice.service.ResumeFileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class MinioController {
    private final ResumeFileService resumeFileService;

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UploadResumeResponse uploadResume(@RequestPart("file")MultipartFile file, @RequestParam(value = "replacedField", required = false) UUID replacedField){
        return resumeFileService.uploadResume(file, replacedField);
    }
}
