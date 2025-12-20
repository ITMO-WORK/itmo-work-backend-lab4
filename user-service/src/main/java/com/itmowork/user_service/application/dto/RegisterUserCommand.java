package com.itmowork.user_service.application.dto;

public record RegisterUserCommand(
        String fullName,
        String password,
        String email
) {}