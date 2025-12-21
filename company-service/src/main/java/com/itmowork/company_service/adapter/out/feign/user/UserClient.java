package com.itmowork.company_service.adapter.out.feign.user;

import com.itmowork.company_service.adapter.out.feign.user.dto.request.UserRequestDto;
import com.itmowork.company_service.adapter.out.feign.user.dto.response.UserResponseDto;
import com.itmowork.company_service.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

    @PostMapping("/api/auth/register-company-owner")
    UserResponseDto registerCompanyOwner(@RequestBody UserRequestDto userRequestDto, @RequestHeader(HttpHeaders.AUTHORIZATION) String token);

    @GetMapping("/api/user/{id}")
    UserResponseDto findUserById(@PathVariable UUID id);
}
