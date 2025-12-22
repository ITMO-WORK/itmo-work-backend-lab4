package org.ilestegor.applicationservice.adapter.output.kafka.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.company.dto.CompanyResultResponse;
import org.ilestegor.applicationservice.adapter.output.kafka.common.ReplyTo;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestType;
import org.ilestegor.applicationservice.adapter.output.kafka.common.SpringKafkaProducer;
import org.ilestegor.applicationservice.adapter.output.kafka.common.registry.CorrelationRegistry;
import org.ilestegor.applicationservice.adapter.output.kafka.common.util.KafkaHeaders;
import org.ilestegor.applicationservice.adapter.output.kafka.company.dto.ValidateCompanyOwnerShipPayload;
import org.ilestegor.applicationservice.adapter.output.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.application.port.output.CompanyPort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class CompanyKafkaAdapter implements CompanyPort {

    private final SpringKafkaProducer springKafkaProducer;
    private final KafkaProps kafkaProps;
    private final CorrelationRegistry correlationRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Boolean> isUserBelongsToCompany(UUID companyId, UUID userId, String token) {
        if (token == null || token.isBlank()){
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        UUID cid = UUID.randomUUID();

        RequestMessage requestMessage = RequestMessage.builder()
                .eventType(RequestType.COMPANY_VALIDATE_OWNERSHIP)
                .correlationId(cid)
                .replyTo(ReplyTo.APPLICATION_RESPONSE)
                .payload(objectMapper.valueToTree(new ValidateCompanyOwnerShipPayload(companyId, userId)))
                .build();

        Mono<CompanyResultResponse> wait =
                withTimeoutHandling(correlationRegistry.registerRequest(cid, kafkaProps.timeout(), CompanyResultResponse.class), RequestType.COMPANY_VALIDATE_OWNERSHIP.getValue());

        return springKafkaProducer
                .send(kafkaProps.topics().companyRequest(), companyId.toString(), requestMessage, KafkaHeaders.withJwt(token))
                .then(wait.map(CompanyResultResponse::result));
    }

    private <T> Mono<T> withTimeoutHandling(Mono<T> mono, String operation) {
        return mono.onErrorMap(
                TimeoutException.class,
                e -> new org.ilestegor.applicationservice.exception.exceptions.TimeoutException("Timeout waiting company-service response for operation=" + operation)
        );
    }
}
