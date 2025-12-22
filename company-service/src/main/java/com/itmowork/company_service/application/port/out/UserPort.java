package com.itmowork.company_service.application.port.out;

import com.itmowork.company_service.adapter.out.kafka.user.dto.UserRequestPayLoad;
import com.itmowork.company_service.adapter.in.kafka.user.responseListener.dto.UserResponsePayLoad;


public interface UserPort {

    UserResponsePayLoad registerCompanyOwner(UserRequestPayLoad userRequestPayLoad, String token);
}
