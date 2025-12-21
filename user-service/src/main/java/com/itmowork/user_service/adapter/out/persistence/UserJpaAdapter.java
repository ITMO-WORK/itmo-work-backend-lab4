package com.itmowork.user_service.adapter.out.persistence;

import com.itmowork.user_service.application.port.out.UserRepositoryPort;
import com.itmowork.user_service.adapter.out.persistence.repository.UserRepository;
import com.itmowork.user_service.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserJpaAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    @Override
    public Mono<User> findById(UUID id) {
        return Mono.fromCallable(() -> userRepository.findById(id).orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(u -> u == null ? Mono.empty() : Mono.just(u));
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return Mono.fromCallable(() -> userRepository.findUserByEmail(email).orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(u -> u == null ? Mono.empty() : Mono.just(u));
    }

    @Override
    public Mono<User> save(User user) {
        return Mono.fromCallable(() -> userRepository.save(user))
                .subscribeOn(Schedulers.boundedElastic());
    }
}