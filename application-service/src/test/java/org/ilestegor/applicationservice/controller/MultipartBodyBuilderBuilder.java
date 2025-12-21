package org.ilestegor.applicationservice.controller;

import org.ilestegor.applicationservice.dto.request.ApplicationCreateRequestDto;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

public final class MultipartBodyBuilderBuilder {

    public static BodyInserter<?, ? super ClientHttpRequest> applicationCreate(String coverLetter) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part(
                "data",
                new ApplicationCreateRequestDto(coverLetter),
                MediaType.APPLICATION_JSON
        );
        return BodyInserters.fromMultipartData(builder.build());
    }

    private MultipartBodyBuilderBuilder() {}
}
