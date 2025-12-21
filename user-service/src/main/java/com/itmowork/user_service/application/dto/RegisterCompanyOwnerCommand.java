package com.itmowork.user_service.application.dto;

public record RegisterCompanyOwnerCommand(
        String fullName,
        String password,
        String email
) {}