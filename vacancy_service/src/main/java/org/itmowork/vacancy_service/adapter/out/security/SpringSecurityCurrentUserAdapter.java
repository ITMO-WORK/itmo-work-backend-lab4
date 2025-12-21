package org.itmowork.vacancy_service.adapter.out.security;

import org.itmowork.vacancy_service.application.port.out.CurrentUserPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }
}