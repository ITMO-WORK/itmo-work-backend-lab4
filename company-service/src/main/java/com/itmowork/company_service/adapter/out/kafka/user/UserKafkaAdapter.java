package com.itmowork.company_service.adapter.out.kafka.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmowork.company_service.adapter.out.kafka.common.util.KafkaHeaders;
import com.itmowork.company_service.adapter.out.kafka.user.dto.UserRequestPayLoad;
import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.UserResponsePayLoad;
import com.itmowork.company_service.adapter.out.kafka.common.ReplyTo;
import com.itmowork.company_service.adapter.out.kafka.common.RequestMessage;
import com.itmowork.company_service.adapter.out.kafka.common.RequestType;
import com.itmowork.company_service.adapter.out.kafka.common.SpringKafkaProducer;
import com.itmowork.company_service.adapter.out.kafka.common.registry.CorrelationRegistry;
import com.itmowork.company_service.adapter.out.kafka.config.KafkaProps;
import com.itmowork.company_service.application.port.out.UserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserKafkaAdapter implements UserPort {

    private final SpringKafkaProducer springKafkaProducer;
    private final KafkaProps kafkaProps;
    private final CorrelationRegistry correlationRegistry;
    private final ObjectMapper objectMapper;


    @Override
    public UserResponsePayLoad registerCompanyOwner(UserRequestPayLoad userRequestPayLoad, String token) {
        UUID correlationId = UUID.randomUUID();

        RequestMessage requestMessage = RequestMessage.builder()
                .eventType(RequestType.USER_CREATE_EVENT)
                .correlationId(correlationId)
                .replyTo(ReplyTo.COMPANY_RESPONSE)
                .payload(objectMapper.valueToTree(userRequestPayLoad))
                .build();

        log.info("Sending USER_CREATE_EVENT request with correlationId={}", correlationId);
        log.info(requestMessage + "");
        Mono<UserResponsePayLoad> wait =
                withTimeoutHandling(correlationRegistry.registerRequest(correlationId, kafkaProps.timeout(), UserResponsePayLoad.class), RequestType.USER_CREATE_EVENT.getName());
        log.debug(wait.toString());
        return springKafkaProducer.send(kafkaProps.topics().userRequest(), requestMessage, KafkaHeaders.withJwt(token))
                .then(wait)
                .block();
    }

    private <T> Mono<T> withTimeoutHandling(Mono<T> mono, String operation) {
        return mono.onErrorMap(
                TimeoutException.class,
                e -> new TimeoutException(
                        "Timeout waiting vacancy-service response for operation=" + operation
                )
        );
    }
}
