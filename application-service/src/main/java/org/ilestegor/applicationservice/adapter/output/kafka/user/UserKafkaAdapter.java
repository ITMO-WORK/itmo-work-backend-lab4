package org.ilestegor.applicationservice.adapter.output.kafka.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.user.dto.UserResponseDto;
import org.ilestegor.applicationservice.adapter.output.kafka.common.ReplyTo;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestType;
import org.ilestegor.applicationservice.adapter.output.kafka.common.SpringKafkaProducer;
import org.ilestegor.applicationservice.adapter.output.kafka.common.registry.CorrelationRegistry;
import org.ilestegor.applicationservice.adapter.output.kafka.common.util.KafkaHeaders;
import org.ilestegor.applicationservice.adapter.output.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.adapter.output.kafka.user.dto.UserExistsPayload;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class UserKafkaAdapter implements UserPort {

    private final SpringKafkaProducer springKafkaProducer;
    private final KafkaProps kafkaProps;
    private final CorrelationRegistry correlationRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<UserResponseDto> checkUserExists(UUID userId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        UUID cid = UUID.randomUUID();

        RequestMessage request = RequestMessage.builder()
                .eventType(RequestType.USER_EXISTS_EVENT)
                .correlationId(cid)
                .replyTo(ReplyTo.APPLICATION_RESPONSE)
                .payload(objectMapper.valueToTree(new UserExistsPayload(userId)))
                .build();

        Mono<UserResponseDto> wait =
                withTimeoutHandling(correlationRegistry.registerRequest(cid, kafkaProps.timeout(), UserResponseDto.class), RequestType.USER_EXISTS_EVENT.getValue());

        return springKafkaProducer
                .send(kafkaProps.topics().userRequest(), userId.toString(), request, KafkaHeaders.withJwt(token))
                .then(wait);
    }


    private <T> Mono<T> withTimeoutHandling(Mono<T> mono, String operation) {
        return mono.onErrorMap(
                TimeoutException.class,
                e -> new org.ilestegor.applicationservice.exception.exceptions.TimeoutException(
                        "Timeout waiting user-service response for operation=" + operation
                )
        );
    }
}
