package com.itmowork.user_service.adapter.out.security;

import com.itmowork.user_service.application.port.out.AuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SpringReactiveAuthAdapter implements AuthPort {

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Override
    public Mono<Void> authenticate(String email, String password) {
        Authentication token = new UsernamePasswordAuthenticationToken(email, password);
        return reactiveAuthenticationManager.authenticate(token).then();
    }
}