package org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto;

import com.fasterxml.jackson.databind.JsonNode;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestType;

import java.util.UUID;

public record ResponseMessage(
        UUID correlationId,
        RequestType eventType,
        boolean ok,
        JsonNode payload,
        JsonNode errorPayload
) {
}
