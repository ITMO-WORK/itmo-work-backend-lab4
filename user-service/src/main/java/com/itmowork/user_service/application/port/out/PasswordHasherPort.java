package com.itmowork.user_service.application.port.out;

public interface PasswordHasherPort {
    String encode(String rawPassword);
}
