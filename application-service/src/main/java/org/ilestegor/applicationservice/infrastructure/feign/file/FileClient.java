package org.ilestegor.applicationservice.infrastructure.feign.file;

import org.ilestegor.applicationservice.configuration.FileFeignConfiguration;
import org.ilestegor.applicationservice.infrastructure.feign.file.dto.UploadResumeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@FeignClient(name = "file-service", path = "/api/files", configuration = FileFeignConfiguration.class)
public interface FileClient {

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    UploadResumeResponse uploadResume(@RequestPart("file") Resource file, @RequestParam(value = "replacedField", required = false) UUID replacedField);
}
