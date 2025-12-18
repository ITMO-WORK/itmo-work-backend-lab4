package org.itmo.work.fileservice.infrastructure.dto;

import lombok.Getter;

@Getter
public enum EventType {

    RESUME_UPLOAD_EVENT("file_upload");

    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
