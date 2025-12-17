package org.ilestegor.applicationservice.infrastructure.kafka.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public enum EventType {
    APPLICATION_STATUS_CHANGE("application_status_change");

    private final String value;

    EventType(String value) {
        this.value = value;
    }
}
