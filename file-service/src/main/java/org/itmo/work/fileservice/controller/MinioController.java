package org.itmo.work.fileservice.controller;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.dto.request.UploadRequest;
import org.itmo.work.fileservice.dto.response.UploadResumeResponse;
import org.itmo.work.fileservice.service.ResumeFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class MinioController {
    private final ResumeFileService resumeFileService;

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResumeResponse> uploadResume(@RequestPart("file") MultipartFile file, @RequestParam(value = "replacedField", required = false) UUID replacedField, @RequestPart("data") UploadRequest uploadRequest) {
        return new ResponseEntity<>(resumeFileService.uploadResume(file, replacedField, uploadRequest), HttpStatus.OK);
    }

    @GetMapping(value = "/resume/{id}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable UUID id) {

        String url = resumeFileService.getDownloadUrl(id);

        return ResponseEntity.ok(
                Map.of("url", url)
        );
    }
}
