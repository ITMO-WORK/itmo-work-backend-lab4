package org.ilestegor.applicationservice.adapter.output.kafka.company.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ValidateCompanyOwnerShipPayload(
        UUID companyId,
        UUID userId
) {
}
