package com.itmowork.user_service.application.dto;

public record LoginCommand(
        String email,
        String password
) {}