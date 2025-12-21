package org.itmo.work.fileservice.config.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.itmo.work.fileservice.config.JwtService;
import org.itmo.work.fileservice.config.UserPrincipal;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.getClaimsFromToken(token);

            String email = claims.getSubject();
            Object userIdRaw = claims.get("userId");
            Object rolesRaw = claims.get("roles");

            if (email == null || email.isBlank() || userIdRaw == null) {
                filterChain.doFilter(request, response);
                return;
            }

            UUID userId = UUID.fromString(userIdRaw.toString());

            List<String> roles = (rolesRaw instanceof List<?> list)
                    ? list.stream().map(Object::toString).toList()
                    : List.of();

            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var principal = new UserPrincipal(email, userId);

                var authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Invalid or expired token", ex);
        }
    }
}
