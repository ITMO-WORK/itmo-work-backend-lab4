package com.itmowork.user_service.adapter.in.web.mapper;

import com.itmowork.user_service.application.dto.query.GetUserByIdQuery;
import com.itmowork.user_service.application.dto.result.UserResult;
import com.itmowork.user_service.adapter.in.web.dto.response.UserResponseDto;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserWebMapper {

    public GetUserByIdQuery toQuery(UUID id) {
        return new GetUserByIdQuery(id);
    }

    public UserResponseDto toResponse(UserResult result) {
        return new UserResponseDto(result.id(), result.fullName(), result.email());
    }
}
