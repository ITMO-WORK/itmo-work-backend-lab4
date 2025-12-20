package org.ilestegor.applicationservice.adapter.input.web.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequestDto(

        @Size(max = 4000)
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        String coverLetter
) {
}

