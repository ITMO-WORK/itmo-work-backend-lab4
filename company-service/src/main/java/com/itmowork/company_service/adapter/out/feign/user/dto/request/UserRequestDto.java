package com.itmowork.company_service.adapter.out.feign.user.dto.request;


public record UserRequestDto(
        String fullName,
        String password,
        String email
) {
}
