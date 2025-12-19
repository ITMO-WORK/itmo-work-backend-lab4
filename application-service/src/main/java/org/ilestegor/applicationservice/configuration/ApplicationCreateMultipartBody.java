package org.ilestegor.applicationservice.configuration;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ilestegor.applicationservice.dto.request.ApplicationCreateRequestDto;

@Schema(name = "ApplicationCreateMultipartBody")
public class ApplicationCreateMultipartBody {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, implementation = ApplicationCreateRequestDto.class)
    public ApplicationCreateRequestDto data;

    @Schema(
            type = "string",
            format = "binary",
            description = "PDF resume file"
    )
    public String resume;
}

