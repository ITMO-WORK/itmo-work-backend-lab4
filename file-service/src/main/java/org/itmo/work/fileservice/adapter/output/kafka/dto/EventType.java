package org.itmo.work.fileservice.adapter.output.kafka.dto;

import lombok.Getter;

@Getter
public enum EventType {

    RESUME_UPLOAD_EVENT("file_upload"),
    APPLICATION_CREATE("application_create");

    private final String name;

    EventType(String name) {
        this.name = name;
    }
}
