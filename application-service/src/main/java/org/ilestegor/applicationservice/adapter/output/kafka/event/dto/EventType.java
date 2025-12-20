package org.ilestegor.applicationservice.adapter.output.kafka.event.dto;

import lombok.Getter;

@Getter
public enum EventType {
    APPLICATION_STATUS_CHANGE("application_status_change"),
    APPLICATION_CREATE("application_create"),
    RESUME_UPLOAD_EVENT("file_upload");

    private final String value;

    EventType(String value) {
        this.value = value;
    }
}
