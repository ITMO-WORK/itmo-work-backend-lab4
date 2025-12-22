package com.itmowork.company_service.adapter.out.kafka.common.registry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class CorrelationRegistry {
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public boolean isPending(UUID correlationId) {
        return pending.containsKey(correlationId);
    }

    public <T> Mono<T> registerRequest(UUID correlationId, Duration timeout, Class<T> type) {
        var sink = Sinks.one();
        pending.put(correlationId, new Pending(sink, type));

        return sink.asMono()
                .timeout(timeout)
                .doFinally(sig -> pending.remove(correlationId))
                .map(type::cast);
    }

    public void complete(UUID correlationId, JsonNode payload) {
        Pending p = pending.get(correlationId);
        if (p == null) return;
        try {
            Object parsed = objectMapper.treeToValue(payload, p.expectedType());
            p.sink().tryEmitValue(parsed);
        } catch (Exception e) {
            p.sink().tryEmitError(e);
        }
    }

    public void fail(UUID correlationId, Throwable error) {
        Pending p = pending.get(correlationId);
        if (p == null) return;
        p.sink().tryEmitError(error);
    }

    private record Pending(Sinks.One<Object> sink, Class<?> expectedType) {
    }
}