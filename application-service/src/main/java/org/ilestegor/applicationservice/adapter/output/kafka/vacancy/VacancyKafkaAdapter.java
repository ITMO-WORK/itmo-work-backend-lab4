package org.ilestegor.applicationservice.adapter.output.kafka.vacancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto.VacancyCompanyIdPayload;
import org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto.VacancyResultPayload;
import org.ilestegor.applicationservice.adapter.input.kafka.responselistener.dto.VacancyTitlePayload;
import org.ilestegor.applicationservice.adapter.output.kafka.common.SpringKafkaProducer;
import org.ilestegor.applicationservice.adapter.output.kafka.common.ReplyTo;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestMessage;
import org.ilestegor.applicationservice.adapter.output.kafka.common.RequestType;
import org.ilestegor.applicationservice.adapter.output.kafka.common.registry.CorrelationRegistry;
import org.ilestegor.applicationservice.adapter.output.kafka.common.util.KafkaHeaders;
import org.ilestegor.applicationservice.adapter.output.kafka.config.KafkaProps;
import org.ilestegor.applicationservice.adapter.output.kafka.vacancy.dto.VacancyIdPayload;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.ilestegor.applicationservice.exception.exceptions.IllegalJsonFormatException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VacancyKafkaAdapter implements VacancyPort {

    private final SpringKafkaProducer springKafkaProducer;
    private final KafkaProps kafkaProps;
    private final CorrelationRegistry correlationRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Boolean> checkVacancyExists(UUID vacancyId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        UUID correlationId = UUID.randomUUID();

        RequestMessage requestMessage = RequestMessage.builder()
                .eventType(RequestType.VACANCY_EXISTS)
                .correlationId(correlationId)
                .replyTo(ReplyTo.APPLICATION_RESPONSE)
                .payload(objectMapper.valueToTree(new VacancyIdPayload(vacancyId)))
                .build();
        Mono<VacancyResultPayload> wait = correlationRegistry.registerRequest(correlationId, kafkaProps.timeout(), VacancyResultPayload.class);

        return springKafkaProducer.send(kafkaProps.topics().vacancyRequest(), vacancyId.toString(), requestMessage, KafkaHeaders.withJwt(token))
                .then(wait.map(VacancyResultPayload::result));
    }

    @Override
    public Mono<Boolean> checkVacancyIsPublished(UUID vacancyId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }
        UUID correlationId = UUID.randomUUID();

        RequestMessage requestMessage = RequestMessage.builder()
                .eventType(RequestType.VACANCY_IS_PUBLISHED)
                .correlationId(correlationId)
                .replyTo(ReplyTo.APPLICATION_RESPONSE)
                .payload(objectMapper.valueToTree(new VacancyIdPayload(vacancyId)))
                .build();

        Mono<VacancyResultPayload> wait =
                correlationRegistry.registerRequest(correlationId, kafkaProps.timeout(), VacancyResultPayload.class);

        return springKafkaProducer.send(kafkaProps.topics().vacancyRequest(), vacancyId.toString(), requestMessage, KafkaHeaders.withJwt(token))
                .then(wait.map(VacancyResultPayload::result));
    }

    @Override
    public Mono<String> getVacancyTitle(UUID vacancyId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        UUID correlationId = UUID.randomUUID();

        RequestMessage requestMessage = RequestMessage.builder()
                .eventType(RequestType.VACANCY_TITLE)
                .correlationId(correlationId)
                .replyTo(ReplyTo.APPLICATION_RESPONSE)
                .payload(objectMapper.valueToTree(new VacancyIdPayload(vacancyId)))
                .build();

        Mono<VacancyTitlePayload> wait =
                correlationRegistry.registerRequest(correlationId, kafkaProps.timeout(), VacancyTitlePayload.class);

        return springKafkaProducer.send(kafkaProps.topics().vacancyRequest(), vacancyId.toString(), requestMessage, KafkaHeaders.withJwt(token))
                .then(wait.map(VacancyTitlePayload::title));
    }

    @Override
    public Mono<UUID> getCompanyIdByVacancyId(UUID vacancyId, String token) {
        if (token == null || token.isBlank()) {
            return Mono.error(new BadCredentialsException("Not authorized"));
        }

        UUID correlationId = UUID.randomUUID();

        RequestMessage requestMessage = RequestMessage.builder()
                .eventType(RequestType.VACANCY_COMPANY_ID)
                .correlationId(correlationId)
                .replyTo(ReplyTo.APPLICATION_RESPONSE)
                .payload(objectMapper.valueToTree(new VacancyIdPayload(vacancyId)))
                .build();

        Mono<VacancyCompanyIdPayload> wait =
                correlationRegistry.registerRequest(correlationId, kafkaProps.timeout(), VacancyCompanyIdPayload.class);

        return springKafkaProducer.send(kafkaProps.topics().vacancyRequest(), vacancyId.toString(), requestMessage, KafkaHeaders.withJwt(token))
                .then(wait.handle((p, sink) -> {
                    if (p.companyId() == null) {
                        sink.error(new IllegalJsonFormatException());
                        return;
                    }
                    sink.next(p.companyId());
                }));
    }
}
