package org.ilestegor.applicationservice.infrastructure.feign.file;

import org.ilestegor.applicationservice.infrastructure.feign.file.dto.UploadResumeResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class FileWebClient {

    private final WebClient webClient;

    public FileWebClient(@LoadBalanced WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://file-service/api/files")
                .build();
    }

    public Mono<UploadResumeResponse> uploadResume(
            FilePart file,
            UUID replacedField
    ) {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", file)
                .filename(file.filename())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        return webClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.path("/resume");
                    if (replacedField != null) {
                        uriBuilder.queryParam("replacedField", replacedField);
                    }
                    return uriBuilder.build();
                })
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(UploadResumeResponse.class);
    }
}
