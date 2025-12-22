package org.ilestegor.applicationservice.adapter.input.kafka.company.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CompanyResultResponse(
        UUID companyId,
        boolean result
) {
}
