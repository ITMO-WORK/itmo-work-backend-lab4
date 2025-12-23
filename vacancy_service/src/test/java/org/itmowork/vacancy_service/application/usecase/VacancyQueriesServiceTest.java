package org.itmowork.vacancy_service.application.usecase;

import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.itmowork.vacancy_service.domain.exception.exceptions.VacancyNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyQueriesServiceTest {

    @Mock VacancyRepositoryPort vacancyRepositoryPort;
    @InjectMocks VacancyQueriesService service;

    @Test
    void getCompanyId_returnsValue() {
        UUID vacancyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(vacancyRepositoryPort.findCompanyId(vacancyId)).thenReturn(companyId);

        assertThat(service.getCompanyId(vacancyId)).isEqualTo(companyId);
    }

    @Test
    void getCompanyId_whenNull_throws() {
        UUID vacancyId = UUID.randomUUID();
        when(vacancyRepositoryPort.findCompanyId(vacancyId)).thenReturn(null);

        assertThatThrownBy(() -> service.getCompanyId(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void getTitle_returnsValue() {
        UUID vacancyId = UUID.randomUUID();
        when(vacancyRepositoryPort.findTitle(vacancyId)).thenReturn("Hello");

        assertThat(service.getTitle(vacancyId)).isEqualTo("Hello");
    }

    @Test
    void getTitle_whenNull_throws() {
        UUID vacancyId = UUID.randomUUID();
        when(vacancyRepositoryPort.findTitle(vacancyId)).thenReturn(null);

        assertThatThrownBy(() -> service.getTitle(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void isPublished_returnsValue() {
        UUID vacancyId = UUID.randomUUID();
        when(vacancyRepositoryPort.isPublished(vacancyId)).thenReturn(true);

        assertThat(service.isPublished(vacancyId)).isTrue();
    }

    @Test
    void isPublished_whenNull_throws() {
        UUID vacancyId = UUID.randomUUID();
        when(vacancyRepositoryPort.isPublished(vacancyId)).thenReturn(null);

        assertThatThrownBy(() -> service.isPublished(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void exists_delegatesToRepo() {
        UUID vacancyId = UUID.randomUUID();
        when(vacancyRepositoryPort.existsById(vacancyId)).thenReturn(true);

        assertThat(service.exists(vacancyId)).isTrue();
        verify(vacancyRepositoryPort).existsById(vacancyId);
    }
}
