package org.ilestegor.applicationservice.adapter.input.kafka.responselistener;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto.ResponseMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.common.ErrorPayload;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.common.registry.CorrelationRegistry;
import org.ilestegor.applicationservice.adapter.output.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.adapter.output.kafka.event.dto.EventMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationResponseListener {

    private final ResponseDispatcher responseDispatcher;

    @KafkaListener(
            topics = "${app.kafka.topics.application-response}"
    )
    public void onMessage(ResponseMessage responseMessage){
       responseDispatcher.dispatch(responseMessage);
    }
}
