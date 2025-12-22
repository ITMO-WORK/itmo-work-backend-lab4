package com.itmowork.company_service.adapter.in.kafka.user.responseListener;

import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.ResponseMessage;
import com.itmowork.company_service.adapter.out.kafka.common.registry.CorrelationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ResponseDispatcher {

    private final CorrelationRegistry correlationRegistry;
//    private final ErrorToExceptionMapper errorToExceptionMapper;

    public void dispatch(ResponseMessage responseMessage) {
        UUID cid = responseMessage.correlationId();
        if (!correlationRegistry.isPending(cid)) {
            return;
        }

        if (responseMessage.ok()) {
            correlationRegistry.complete(cid, responseMessage.payload());
            return;
        }


//        var exception = errorToExceptionMapper.toException(responseMessage.eventType(), responseMessage.errorPayload());
//        correlationRegistry.fail(cid, exception);
    }
}
