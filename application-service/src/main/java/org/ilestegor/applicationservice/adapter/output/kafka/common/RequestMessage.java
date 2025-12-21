package org.ilestegor.applicationservice.adapter.output.kafka.common;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record RequestMessage(
        RequestType eventType,
        UUID correlationId,
        ReplyTo replyTo,
        JsonNode payload
) {
}
