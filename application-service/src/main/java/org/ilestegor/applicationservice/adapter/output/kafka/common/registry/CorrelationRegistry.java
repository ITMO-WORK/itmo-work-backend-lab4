package org.ilestegor.applicationservice.adapter.output.kafka.common.registry;

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
public class CorrelationRegistry {
    private record Pending(Sinks.One<Object> sink, Class<?> expectedType){}
    private final Map<UUID, Pending> pending = new ConcurrentHashMap<>();


    public boolean isPending(UUID correlationId){
        return pending.containsKey(correlationId);
    }

    public <T> Mono<T> registerRequest(UUID correlationId, Duration timeout, Class<T> type){
        var sink = Sinks.one();
        pending.put(correlationId, new Pending(sink, type));

        return sink.asMono()
                .timeout(timeout)
                .doFinally(sig -> pending.remove(correlationId))
                .map(type::cast);
    }

    public void complete(UUID correlationId, Object payload) {
        Pending p = pending.get(correlationId);
        if (p == null) return;
        p.sink().tryEmitValue(payload);
    }

    public void fail(UUID correlationId, Throwable error) {
        Pending p = pending.get(correlationId);
        if (p == null) return;
        p.sink().tryEmitError(error);
    }
}
