package com.itmowork.user_service.adapter.in.web.mapper;

import com.itmowork.user_service.adapter.in.web.dto.request.LoginRequestDto;
import com.itmowork.user_service.adapter.in.web.dto.request.UserRequestDto;
import com.itmowork.user_service.adapter.in.web.dto.response.AuthResponseDto;
import com.itmowork.user_service.application.dto.LoginCommand;
import com.itmowork.user_service.application.dto.RegisterCompanyOwnerCommand;
import com.itmowork.user_service.application.dto.RegisterUserCommand;
import com.itmowork.user_service.application.dto.result.AuthResult;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    public RegisterCompanyOwnerCommand toRegisterCompanyOwnerCommand(UserRequestDto dto) {
        return new RegisterCompanyOwnerCommand(dto.fullName(), dto.password(), dto.email());
    }

    public LoginCommand toLoginCommand(LoginRequestDto dto) {
        return new LoginCommand(dto.email(), dto.password());
    }

    public RegisterUserCommand toRegisterCommand(UserRequestDto dto) {
        return new RegisterUserCommand(dto.fullName(), dto.password(), dto.email());
    }

    public AuthResponseDto toResponse(AuthResult result) {
        return new AuthResponseDto(result.id(), result.token());
    }
}