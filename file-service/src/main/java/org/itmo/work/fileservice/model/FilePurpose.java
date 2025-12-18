package org.itmo.work.fileservice.model;

import lombok.Getter;

@Getter
public enum FilePurpose {
    APPLICATION_RESUME("application_resume");

    private String value;

    FilePurpose(String value) {
        this.value = value;
    }
}