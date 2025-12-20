package com.itmowork.user_service.config;

import com.itmowork.user_service.application.port.in.GetUserByIdUseCase;
import com.itmowork.user_service.application.port.in.LoginUseCase;
import com.itmowork.user_service.application.port.in.RegisterCompanyOwnerUseCase;
import com.itmowork.user_service.application.port.in.RegisterUserUseCase;
import com.itmowork.user_service.application.port.out.*;
import com.itmowork.user_service.application.usecase.GetUserByIdService;
import com.itmowork.user_service.application.usecase.LoginService;
import com.itmowork.user_service.application.usecase.RegisterCompanyOwnerService;
import com.itmowork.user_service.application.usecase.RegisterUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterCompanyOwnerUseCase registerCompanyOwnerUseCase(
            UserRepositoryPort userRepositoryPort,
            RoleRepositoryPort roleRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenGeneratorPort tokenGeneratorPort
    ) {
        return new RegisterCompanyOwnerService(
                userRepositoryPort,
                roleRepositoryPort,
                passwordHasherPort,
                tokenGeneratorPort
        );
    }

    @Bean
    public LoginUseCase loginUseCase(
            AuthPort authPort,
            UserRepositoryPort userRepositoryPort,
            TokenGeneratorPort tokenGeneratorPort
    ) {
        return new LoginService(authPort, userRepositoryPort, tokenGeneratorPort);
    }


    @Bean
    public RegisterUserUseCase registerUserUseCase(
            UserRepositoryPort userRepositoryPort,
            RoleRepositoryPort roleRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenGeneratorPort tokenGeneratorPort
    ) {
        return new RegisterUserService(
                userRepositoryPort,
                roleRepositoryPort,
                passwordHasherPort,
                tokenGeneratorPort
        );
    }

    @Bean
    public GetUserByIdUseCase getUserByIdUseCase(UserRepositoryPort userRepositoryPort) {
        return new GetUserByIdService(userRepositoryPort);
    }
}