package com.itmowork.user_service.application.port.out;

import reactor.core.publisher.Mono;

public interface AuthPort {
    Mono<Void> authenticate(String email, String password);
}