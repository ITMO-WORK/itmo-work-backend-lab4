package com.itmowork.company_service.adapter.in.kafka.user.responseListener;

import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.ResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyResponseListener {
    private final ResponseDispatcher responseDispatcher;

    @KafkaListener(
            topics = "${app.kafka.topics.company-response}",
            containerFactory = "responseMessageKafkaListenerContainerFactory",
            groupId = "company-service"
    )
    public void onMessage(ResponseMessage responseMessage) {
        responseDispatcher.dispatch(responseMessage);
    }
}
