package com.itmowork.user_service.adapter.out.security;

import com.itmowork.user_service.adapter.out.security.jwt.JwtService;
import com.itmowork.user_service.application.port.out.TokenGeneratorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final JwtService jwtService;

    @Override
    public Mono<String> generateAccessToken(UUID userId, String email, List<String> roles) {
        var authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();

        var authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                authorities
        );

        String token = jwtService.generateAccessToken(authentication, userId, roles);
        return Mono.just(token);
    }
}