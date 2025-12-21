package org.ilestegor.applicationservice.adapter.output.security;

import lombok.RequiredArgsConstructor;
import org.ilestegor.applicationservice.application.port.output.CurrentUserPort;
import org.ilestegor.applicationservice.configuration.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public Mono<CurrentUser> getCurrentUser() {
        Mono<UserPrincipal> principalMono = ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserPrincipal.class);

        Mono<String> tokenMono = Mono.deferContextual(ctx -> {
            String token = ctx.getOrDefault("authToken", null);
            if (token == null)
                return Mono.error(new org.springframework.security.authentication.BadCredentialsException("No auth token"));
            return Mono.just(token);
        });

        return Mono.zip(principalMono, tokenMono)
                .map(t -> new CurrentUser(t.getT1().userId(), t.getT2()));
    }
}
