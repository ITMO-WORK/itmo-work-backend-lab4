package org.itmowork.vacancy_service.adapter.out.security.jwt;

import io.jsonwebtoken.Claims;

public interface JwtService {
    Claims parseAllClaims(String token);
}