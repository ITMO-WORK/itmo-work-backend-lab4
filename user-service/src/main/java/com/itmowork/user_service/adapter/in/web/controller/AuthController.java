package com.itmowork.user_service.adapter.in.web.controller;


import com.itmowork.user_service.adapter.in.web.dto.request.LoginRequestDto;
import com.itmowork.user_service.adapter.in.web.dto.request.UserRequestDto;
import com.itmowork.user_service.adapter.in.web.dto.response.AuthResponseDto;
import com.itmowork.user_service.adapter.in.web.mapper.AuthWebMapper;
import com.itmowork.user_service.application.port.in.LoginUseCase;
import com.itmowork.user_service.application.port.in.RegisterCompanyOwnerUseCase;
import com.itmowork.user_service.application.port.in.RegisterUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthWebMapper authWebMapper;
    private final RegisterCompanyOwnerUseCase registerCompanyOwnerUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register")
    public Mono<AuthResponseDto> register(@RequestBody @Valid Mono<UserRequestDto> userRequestDto) {
        return userRequestDto
                .map(authWebMapper::toRegisterCommand)
                .flatMap(registerUserUseCase::register)
                .map(authWebMapper::toResponse);
    }

    @PostMapping("/register-company-owner")
    public Mono<AuthResponseDto> registerCompanyOwner(@RequestBody @Valid Mono<UserRequestDto> userRequestDto) {
        return userRequestDto
                .map(authWebMapper::toRegisterCompanyOwnerCommand)
                .flatMap(registerCompanyOwnerUseCase::registerCompanyOwner)
                .map(authWebMapper::toResponse);
    }

    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody @Valid Mono<LoginRequestDto> loginRequestDto) {
        return loginRequestDto
                .map(authWebMapper::toLoginCommand)
                .flatMap(loginUseCase::login)
                .map(authWebMapper::toResponse);
    }
}