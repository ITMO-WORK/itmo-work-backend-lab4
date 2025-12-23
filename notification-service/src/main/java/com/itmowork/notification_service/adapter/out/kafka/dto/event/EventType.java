package com.itmowork.notification_service.adapter.out.kafka.dto.event;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EventType {
    APPLICATION_STATUS_CHANGE,
    VACANCY_STATUS_CHANGE,
    RESUME_UPLOAD_EVENT,
    @JsonEnumDefaultValue
    UNKNOWN

}
