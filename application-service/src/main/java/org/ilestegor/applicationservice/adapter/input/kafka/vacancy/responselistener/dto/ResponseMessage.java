package org.ilestegor.applicationservice.adapter.input.kafka.vacancy.responselistener.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestType;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ResponseMessage(
        UUID correlationId,
        RequestType eventType,
        boolean ok,
        JsonNode payload,
        JsonNode errorPayload
) {
}
