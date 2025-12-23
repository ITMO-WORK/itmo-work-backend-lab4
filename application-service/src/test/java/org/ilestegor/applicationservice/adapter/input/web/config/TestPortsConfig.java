package org.ilestegor.applicationservice.adapter.input.web.config;

import org.ilestegor.applicationservice.application.port.output.ApplicationEventPublisherPort;
import org.ilestegor.applicationservice.application.port.output.CompanyPort;
import org.ilestegor.applicationservice.application.port.output.UserPort;
import org.ilestegor.applicationservice.application.port.output.VacancyPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestPortsConfig {
    @Bean
    @Primary
    public UserPort userPort() {
        return mock(UserPort.class);
    }

    @Bean
    @Primary
    public VacancyPort vacancyPort() {
        return mock(VacancyPort.class);
    }

    @Bean
    @Primary
    public CompanyPort companyPort() {
        return mock(CompanyPort.class);
    }

    @Bean
    @Primary
    public ApplicationEventPublisherPort applicationEventPublisherPort() {
        return mock(ApplicationEventPublisherPort.class);
    }
}
