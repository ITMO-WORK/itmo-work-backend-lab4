package org.ilestegor.applicationservice.adapter.input.kafka.vacancy.responselistener.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.output.kafka.common.ErrorPayload;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestType;
import org.ilestegor.applicationservice.exception.exceptions.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorToExceptionMapper {
    private final ObjectMapper objectMapper;

    public RuntimeException toException(RequestType requestType, JsonNode errorPayloadNode) {
        ErrorPayload errorPayload;
        try {
            errorPayload = objectMapper.treeToValue(errorPayloadNode, ErrorPayload.class);
        } catch (JsonProcessingException ex) {
            return new IllegalJsonFormatException();
        }

        return switch (errorPayload.code()) {
            case BAD_REQUEST -> new VacancyBadRequest(errorPayload.message());
            case UNSUPPORTED_OPERATION -> new WrongEventException(errorPayload.message());
            case INTERNAL_ERROR -> new VacancyInternalError(errorPayload.message());
            case FORBIDDEN -> new ForbiddenErrorException(errorPayload.message());
            case UNAUTHORIZED -> null;
        };
    }
}
