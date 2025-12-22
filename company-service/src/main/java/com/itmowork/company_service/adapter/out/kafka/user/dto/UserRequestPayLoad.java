package com.itmowork.company_service.adapter.out.kafka.user.dto;


public record UserRequestPayLoad(
        String fullName,
        String password,
        String email
) {
}
