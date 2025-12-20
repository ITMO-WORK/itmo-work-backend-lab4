package com.itmowork.user_service.adapter.out.persistence.repository;

import com.itmowork.user_service.adapter.in.web.dto.response.UserResponseDto;
import com.itmowork.user_service.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findUserByEmail(String email);

    UserResponseDto findUserById(UUID id);
}
