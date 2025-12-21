package com.itmowork.user_service.adapter.out.persistence;

import com.itmowork.user_service.application.port.out.RoleRepositoryPort;
import com.itmowork.user_service.adapter.out.persistence.repository.RoleRepository;
import com.itmowork.user_service.domain.model.Role;
import com.itmowork.user_service.domain.model.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class RoleJpaAdapter implements RoleRepositoryPort {

    private final RoleRepository roleRepository;

    @Override
    public Mono<Role> findByRoleName(RoleName roleName) {
        return Mono.fromCallable(() -> roleRepository.findRoleByRoleName(roleName).orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(r -> r == null ? Mono.empty() : Mono.just(r));
    }
}