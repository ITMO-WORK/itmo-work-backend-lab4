package com.itmowork.user_service.adapter.out.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(Topics topics, Duration timeout)
{
    public record Topics(String userRequest, String companyResponse) {}
}