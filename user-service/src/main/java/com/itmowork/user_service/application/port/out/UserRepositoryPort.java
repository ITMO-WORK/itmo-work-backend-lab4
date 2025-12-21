package com.itmowork.user_service.application.port.out;


import com.itmowork.user_service.domain.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepositoryPort {
    Mono<User> findById(UUID id);
    Mono<User> findByEmail(String email);
    Mono<User> save(User user);
}