package org.ilestegor.applicationservice.infrastructure.feign.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.infrastructure.feign.file.dto.DownloadUrlResponse;
import org.ilestegor.applicationservice.infrastructure.feign.file.dto.UploadRequest;
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
    private final ObjectMapper objectMapper;

    public FileWebClient(@LoadBalanced WebClient.Builder builder, ObjectMapper objectMapper) {
        this.webClient = builder
                .baseUrl("http://file-service/api/files")
                .build();
        this.objectMapper = objectMapper;
    }

    public Mono<UploadResumeResponse> uploadResume(FilePart file, UUID replacedField, UploadRequest uploadRequest)  {
        MultipartBodyBuilder b = new MultipartBodyBuilder();

        b.part("file", file)
                .filename(file.filename())
                .contentType(MediaType.APPLICATION_PDF);

        try {
            b.part("data", objectMapper.writeValueAsString(uploadRequest))
                    .contentType(MediaType.APPLICATION_JSON);
            System.out.println(new ObjectMapper().writeValueAsString(uploadRequest));
        } catch (JsonProcessingException ex){
            throw new IllegalStateException("");
        }

        return webClient.post()
                .uri(uriBuilder -> {
                    uriBuilder.path("/resume");
                    if (replacedField != null) uriBuilder.queryParam("replacedField", replacedField);
                    return uriBuilder.build();
                })
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(b.build()))
                .retrieve()
                .bodyToMono(UploadResumeResponse.class);
    }


    public Mono<String> getResumeDownloadUrl(UUID fileId) {
        return webClient.get()
                .uri("/resume/{id}", fileId)
                .retrieve()
                .bodyToMono(DownloadUrlResponse.class)
                .map(DownloadUrlResponse::url);
    }
}
