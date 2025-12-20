package org.itmo.work.fileservice.adapter.output.security;

import org.itmo.work.fileservice.application.port.output.CurrentUserPort;
import org.itmo.work.fileservice.config.UserPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SpringSecurityCurrentUserAdapter implements CurrentUserPort {

    @Override
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            throw new BadCredentialsException("Not authorized");

        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
        return userPrincipal.userId();
    }
}
