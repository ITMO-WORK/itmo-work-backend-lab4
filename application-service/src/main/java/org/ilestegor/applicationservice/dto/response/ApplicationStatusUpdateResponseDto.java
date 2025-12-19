package org.ilestegor.applicationservice.dirty.dto.response;

import java.time.LocalDateTime;

public record ApplicationStatusUpdateResponseDto (
        String status,
        LocalDateTime updateAt
){}
