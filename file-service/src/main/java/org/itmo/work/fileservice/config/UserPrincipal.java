package org.itmo.work.fileservice.config;

import java.util.UUID;

public record UserPrincipal (
        String email,
        UUID userId
){ }
