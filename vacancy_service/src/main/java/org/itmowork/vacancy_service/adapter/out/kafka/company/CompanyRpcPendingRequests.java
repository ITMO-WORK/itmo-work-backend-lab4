package org.itmowork.vacancy_service.adapter.out.kafka.company;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CompanyRpcPendingRequests {

    private final ConcurrentHashMap<UUID, CompletableFuture<Boolean>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<Boolean> register(UUID correlationId) {
        CompletableFuture<Boolean> f = new CompletableFuture<>();
        pending.put(correlationId, f);
        return f;
    }

    public boolean complete(UUID correlationId, boolean result) {
        CompletableFuture<Boolean> f = pending.remove(correlationId);
        if (f == null) return false;
        f.complete(result);
        return true;
    }

    public boolean fail(UUID correlationId, Throwable ex) {
        CompletableFuture<Boolean> f = pending.remove(correlationId);
        if (f == null) return false;
        f.completeExceptionally(ex);
        return true;
    }

    public void remove(UUID correlationId) {
        pending.remove(correlationId);
    }
}