package org.itmo.work.fileservice.adapter.input.web;

import lombok.RequiredArgsConstructor;
import org.itmo.work.fileservice.application.port.input.GetResumeFilePort;
import org.itmo.work.fileservice.application.port.input.UploadResumePort;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeRequest;
import org.itmo.work.fileservice.application.usecase.uploadresume.dto.UploadResumeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class MinioController {
    private final UploadResumePort uploadResume;
    private final GetResumeFilePort getDownloadUrl;

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResumeResponse> uploadResume(@RequestPart("file") MultipartFile file, @RequestParam(value = "applicationId", required = false) UUID applicationId) {

        return new ResponseEntity<>(uploadResume.uploadResume(file, applicationId), HttpStatus.OK);
    }

    @GetMapping(value = "/resume/{applicationId}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable UUID applicationId) {

        String url = getDownloadUrl.getDownloadUrl(applicationId);

        return ResponseEntity.ok(
                Map.of("url", url)
        );
    }

}
