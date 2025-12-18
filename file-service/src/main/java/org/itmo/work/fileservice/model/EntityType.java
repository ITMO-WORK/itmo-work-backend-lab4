package org.itmo.work.fileservice.model;

import lombok.Getter;

@Getter
public enum EntityType {
    APPLICATION("application");

    private final String value;

    EntityType(String value) {
        this.value = value;
    }
}
