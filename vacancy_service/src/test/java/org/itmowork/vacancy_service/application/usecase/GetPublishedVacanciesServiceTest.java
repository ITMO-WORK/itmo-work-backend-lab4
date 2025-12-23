package org.itmowork.vacancy_service.application.usecase;

import org.itmowork.vacancy_service.application.dto.VacancyResult;
import org.itmowork.vacancy_service.application.dto.query.GetPublishedVacanciesQuery;
import org.itmowork.vacancy_service.application.port.out.VacancyRepositoryPort;
import org.itmowork.vacancy_service.application.usecase.GetPublishedVacanciesService;
import org.itmowork.vacancy_service.domain.model.Currency;
import org.itmowork.vacancy_service.domain.model.Vacancy;
import org.itmowork.vacancy_service.domain.model.VacancyStatus;
import org.itmowork.vacancy_service.domain.model.VacancyStatusName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPublishedVacanciesServiceTest {

    @Mock VacancyRepositoryPort vacancyRepositoryPort;
    @InjectMocks
    GetPublishedVacanciesService service;

    @Test
    void getPublished_mapsPageToVacancyResult() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        Vacancy v1 = Vacancy.builder()
                .id(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .title("t1").description("d1")
                .salaryFrom(10).salaryTo(20)
                .createdAt(LocalDateTime.now())
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.PUBLISHED).build())
                .currency(Currency.builder().id(10L).currency("EUR").build())
                .build();

        Vacancy v2 = Vacancy.builder()
                .id(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .title("t2").description("d2")
                .salaryFrom(30).salaryTo(40)
                .createdAt(LocalDateTime.now())
                .status(VacancyStatus.builder().id(1L).vacancyStatusName(VacancyStatusName.PUBLISHED).build())
                .currency(Currency.builder().id(10L).currency("EUR").build())
                .build();

        Page<Vacancy> page = new PageImpl<>(List.of(v1, v2), pageable, 2);
        when(vacancyRepositoryPort.findPublished(pageable)).thenReturn(page);

        Page<VacancyResult> res = service.getPublished(new GetPublishedVacanciesQuery(pageable));

        assertThat(res.getTotalElements()).isEqualTo(2);
        assertThat(res.getContent()).hasSize(2);
        assertThat(res.getContent().get(0).id()).isEqualTo(v1.getId());

        verify(vacancyRepositoryPort).findPublished(pageable);
    }
}
