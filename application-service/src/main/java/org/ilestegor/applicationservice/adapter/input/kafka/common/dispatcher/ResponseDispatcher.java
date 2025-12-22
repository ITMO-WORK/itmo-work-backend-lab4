package org.ilestegor.applicationservice.adapter.input.kafka.common.dispatcher;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.common.ResponseMessage;
import org.ilestegor.applicationservice.adapter.input.kafka.common.errormapper.ErrorToExceptionMapper;
import org.ilestegor.applicationservice.adapter.output.kafka.common.registry.CorrelationRegistry;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResponseDispatcher {

    private final CorrelationRegistry correlationRegistry;
    private final ErrorToExceptionMapper errorToExceptionMapper;


    public void dispatch(ResponseMessage responseMessage) {
        UUID cid = responseMessage.correlationId();
        if (!correlationRegistry.isPending(cid)) {
            return;
        }

        if (responseMessage.ok()) {
            correlationRegistry.complete(cid, responseMessage.payload());
            return;
        }


        var exception = errorToExceptionMapper.toException(responseMessage.errorPayload());
        correlationRegistry.fail(cid, exception);
    }
}
