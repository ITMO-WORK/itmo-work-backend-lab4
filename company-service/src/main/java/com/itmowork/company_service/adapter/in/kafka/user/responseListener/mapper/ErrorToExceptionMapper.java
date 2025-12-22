package com.itmowork.company_service.adapter.in.kafka.user.responseListener.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.company_service.adapter.out.kafka.common.ErrorPayload;
import com.itmowork.company_service.adapter.out.kafka.common.RequestType;
import com.itmowork.company_service.domain.exception.exceptions.*;
import lombok.RequiredArgsConstructor;
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
            case BAD_REQUEST -> new UserBadRequest(errorPayload.message());
            case UNSUPPORTED_OPERATION -> new WrongEventException(errorPayload.message());
            case INTERNAL_ERROR -> new UserInternalError(errorPayload.message());
            case FORBIDDEN -> new ForbiddenErrorException(errorPayload.message());
            case UNAUTHORIZED -> null;
        };
    }
}
