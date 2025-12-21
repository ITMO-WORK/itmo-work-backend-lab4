package org.ilestegor.applicationservice.adapter.input.kafka.responselistener;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto.ResponseMessage;
import org.ilestegor.applicationservice.adapter.input.kafka.responselistener.mapper.ErrorToExceptionMapper;
import org.ilestegor.applicationservice.adapter.output.kafka.common.registry.CorrelationRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResponseDispatcher {

    private final CorrelationRegistry correlationRegistry;
    private final ErrorToExceptionMapper errorToExceptionMapper;

    public void dispatch(ResponseMessage responseMessage){
        UUID cid = responseMessage.correlationId();
        if (!correlationRegistry.isPending(cid)){
            return;
        }

        if (responseMessage.ok())
            correlationRegistry.complete(cid, responseMessage.payload());

        var exception = errorToExceptionMapper.toException(responseMessage.eventType(), responseMessage.errorPayload());
        correlationRegistry.fail(cid, exception);
    }
}
