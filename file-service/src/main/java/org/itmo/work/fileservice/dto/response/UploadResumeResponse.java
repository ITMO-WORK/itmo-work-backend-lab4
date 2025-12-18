package org.itmo.work.fileservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record UploadResumeResponse(
        @JsonProperty("field_id") UUID fieldId,
        @JsonProperty("replaced_field") UUID replacedField
) {
}
