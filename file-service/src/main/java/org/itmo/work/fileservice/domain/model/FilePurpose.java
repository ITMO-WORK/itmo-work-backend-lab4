package org.itmo.work.fileservice.domain.model;

import lombok.Getter;

@Getter
public enum FilePurpose {
    APPLICATION_RESUME("application_resume");

    private final String value;

    FilePurpose(String value) {
        this.value = value;
    }
}