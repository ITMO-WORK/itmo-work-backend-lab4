package org.ilestegor.applicationservice.adapter.input.kafka.common.errormapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.common.ErrorCode;
import org.ilestegor.applicationservice.adapter.input.kafka.common.ErrorPayload;
import org.ilestegor.applicationservice.exception.exceptions.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ErrorToExceptionMapper {
    private final ObjectMapper objectMapper;


    private static final Map<ErrorCode, java.util.function.Function<String, RuntimeException>> MAP =
            Map.of(
                    ErrorCode.BAD_REQUEST, RemoteBadRequestException::new,
                    ErrorCode.FORBIDDEN, ForbiddenErrorException::new,
                    ErrorCode.UNAUTHORIZED, BadCredentialsException::new,
                    ErrorCode.INTERNAL_ERROR, RemoteServiceException::new,
                    ErrorCode.UNSUPPORTED_OPERATION, WrongEventException::new
            );

    public RuntimeException toException(JsonNode errorPayloadNode) {
        final ErrorPayload payload;
        try {
            payload = objectMapper.treeToValue(errorPayloadNode, ErrorPayload.class);
        } catch (Exception e) {
            return new IllegalJsonFormatException();
        }

        var factory = MAP.get(payload.code());
        if (factory == null) {
            return new RemoteServiceException("Unknown error code=" + payload.code() + " message=" + payload.message());
        }
        return factory.apply(payload.message());
    }
}
