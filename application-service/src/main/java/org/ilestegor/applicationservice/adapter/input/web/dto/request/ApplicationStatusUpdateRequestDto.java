package org.ilestegor.applicationservice.adapter.input.web.dto.request;

import org.ilestegor.applicationservice.domain.ApplicationStatusName;

public record ApplicationStatusUpdateRequestDto(
        ApplicationStatusName applicationStatusName
) {
}
