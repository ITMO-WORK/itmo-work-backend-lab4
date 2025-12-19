package org.ilestegor.applicationservice.dirty.dto.request;

import org.ilestegor.applicationservice.dirty.model.ApplicationStatusName;

public record ApplicationStatusUpdateRequestDto(
        ApplicationStatusName applicationStatusName
){}
