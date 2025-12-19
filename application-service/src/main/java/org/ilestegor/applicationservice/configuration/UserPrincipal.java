package org.ilestegor.applicationservice.dirty.configuration;

import java.util.UUID;

public record UserPrincipal (
        String email,
        UUID userId
){ }
