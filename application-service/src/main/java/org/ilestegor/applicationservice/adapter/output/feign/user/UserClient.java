package org.ilestegor.applicationservice.adapter.output.feign.user;

import org.ilestegor.applicationservice.adapter.output.feign.user.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/user")
public interface UserClient {

    @GetMapping("/{id}")
    UserResponseDto isUserExistsById(@PathVariable UUID id, @RequestHeader(HttpHeaders.AUTHORIZATION) String token);
}

