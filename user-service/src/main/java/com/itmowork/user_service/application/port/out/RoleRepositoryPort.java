package com.itmowork.user_service.application.port.out;

import com.itmowork.user_service.domain.model.Role;
import com.itmowork.user_service.domain.model.RoleName;
import reactor.core.publisher.Mono;

public interface RoleRepositoryPort {
    Mono<Role> findByRoleName(RoleName roleName);
}