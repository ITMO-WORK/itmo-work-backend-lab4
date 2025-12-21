package com.itmowork.company_service.adapter.out.feign.user;

import com.itmowork.company_service.adapter.out.feign.user.dto.request.UserRequestDto;
import com.itmowork.company_service.adapter.out.feign.user.dto.response.UserResponseDto;
import com.itmowork.company_service.application.port.out.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFeignAdapter implements UserPort {

    private final UserClient userClient;

    @Override
    public UserResponseDto registerCompanyOwner(UserRequestDto userRequestDto, String token) {
        return userClient.registerCompanyOwner(userRequestDto, token);
    }
}
