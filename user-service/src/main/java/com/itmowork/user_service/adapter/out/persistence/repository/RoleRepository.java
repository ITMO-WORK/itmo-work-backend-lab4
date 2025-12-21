package com.itmowork.user_service.adapter.out.persistence.repository;

import com.itmowork.user_service.domain.model.Role;
import com.itmowork.user_service.domain.model.RoleName;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findRoleByRoleName(RoleName roleName);
}
