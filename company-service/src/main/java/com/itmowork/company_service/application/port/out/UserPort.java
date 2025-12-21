package com.itmowork.company_service.application.port.out;

import com.itmowork.company_service.adapter.out.feign.user.dto.request.UserRequestDto;
import com.itmowork.company_service.adapter.out.feign.user.dto.response.UserResponseDto;


public interface UserPort {

    UserResponseDto registerCompanyOwner(UserRequestDto userRequestDto, String token);
}
