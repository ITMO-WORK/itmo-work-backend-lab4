package com.itmowork.user_service.application.port.out;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface TokenGeneratorPort {
    Mono<String> generateAccessToken(UUID userId, String email, List<String> roles);
}