package com.itmowork.user_service.adapter.in.web.controller;

import com.itmowork.user_service.adapter.in.web.mapper.UserWebMapper;
import com.itmowork.user_service.application.port.in.GetUserByIdUseCase;
import com.itmowork.user_service.adapter.in.web.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UserWebMapper userWebMapper;

    @GetMapping("/{id}")
    public Mono<UserResponseDto> findUserById(@PathVariable UUID id) {
        return getUserByIdUseCase.getById(userWebMapper.toQuery(id))
                .map(userWebMapper::toResponse);
    }
}
