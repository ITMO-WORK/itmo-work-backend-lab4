package com.itmowork.company_service.adapter.in.web.dto.response;

import java.util.UUID;

public record CompanyDeleteResponseDto(
        UUID id,
        String message
) {
}
