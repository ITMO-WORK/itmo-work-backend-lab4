package org.ilestegor.applicationservice.infrastructure.kafka.common.pending;

import org.ilestegor.applicationservice.infrastructure.kafka.vacancy.dto.response.VacancyResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PendingRequest {

    private final ConcurrentHashMap<UUID, Sinks.One<VacancyResponse>> pendingRequests = new ConcurrentHashMap<>();

    public Mono<VacancyResponse> registerRequest(UUID correlationId, Duration timeout){
        Sinks.One<VacancyResponse> sink = Sinks.one();
        Sinks.One<VacancyResponse> prev = pendingRequests.putIfAbsent(correlationId, sink);
        if (prev != null) {
            return Mono.error(new IllegalStateException("Duplicate correlationId: " + correlationId));
        }

        return sink.asMono()
                .timeout(timeout)
                .doFinally(signal -> pendingRequests.remove(correlationId));
    }

    public void completeRequest(VacancyResponse vacancyResponse){
        if (vacancyResponse == null || vacancyResponse.correlationId() == null) return;

        Sinks.One<VacancyResponse> sink = pendingRequests.remove(vacancyResponse.correlationId());

        if (sink != null)
            sink.tryEmitValue(vacancyResponse);
    }

    public void failRequest(UUID correlationId, Throwable error){
        if (correlationId == null) return;
        Sinks.One<VacancyResponse> sink = pendingRequests.remove(correlationId);
        if (sink != null)
            sink.tryEmitError(error);
    }
}
