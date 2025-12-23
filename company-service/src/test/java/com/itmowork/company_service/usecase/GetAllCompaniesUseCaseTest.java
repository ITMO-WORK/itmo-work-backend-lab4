package com.itmowork.company_service.usecase;

import com.itmowork.company_service.application.usecase.GetAllCompaniesUseCase;
import com.itmowork.company_service.application.port.out.CompanyRepositoryPort;
import com.itmowork.company_service.adapter.in.web.dto.response.CompanyResponseDto;
import com.itmowork.company_service.domain.model.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllCompaniesUseCaseTest {

    @Mock
    CompanyRepositoryPort companyRepositoryPort;

    GetAllCompaniesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllCompaniesUseCase(companyRepositoryPort);
    }

    @Test
    void shouldReturnPagedCompanies() {
        Pageable pageable = PageRequest.of(0, 2);

        Company company1 = new Company();
        company1.setId(UUID.randomUUID());
        company1.setName("ITMO");
        company1.setEmail("info@itmo.ru");
        company1.setDescription("University");

        Company company2 = new Company();
        company2.setId(UUID.randomUUID());
        company2.setName("Google");
        company2.setEmail("info@google.com");
        company2.setDescription("Tech company");

        when(companyRepositoryPort.count())
                .thenReturn(Mono.just(2L));

        when(companyRepositoryPort.findAllCompaniesPaged(2L, 0L))
                .thenReturn(Flux.just(company1, company2));

        StepVerifier.create(useCase.getAllCompanies(pageable))
                .assertNext(page -> {
                    assertThat(page.getTotalElements()).isEqualTo(2);
                    assertThat(page.getContent()).hasSize(2);

                    CompanyResponseDto dto1 = page.getContent().get(0);
                    assertThat(dto1.id()).isEqualTo(company1.getId());
                    assertThat(dto1.name()).isEqualTo("ITMO");
                    assertThat(dto1.email()).isEqualTo("info@itmo.ru");
                    assertThat(dto1.description()).isEqualTo("University");

                    CompanyResponseDto dto2 = page.getContent().get(1);
                    assertThat(dto2.id()).isEqualTo(company2.getId());
                    assertThat(dto2.name()).isEqualTo("Google");
                })
                .verifyComplete();

        verify(companyRepositoryPort).count();
        verify(companyRepositoryPort).findAllCompaniesPaged(2L, 0L);
    }

    @Test
    void shouldReturnEmptyPageWhenNoCompaniesFound() {
        Pageable pageable = PageRequest.of(1, 5);

        when(companyRepositoryPort.count())
                .thenReturn(Mono.just(0L));

        when(companyRepositoryPort.findAllCompaniesPaged(5L, 5L))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.getAllCompanies(pageable))
                .assertNext(page -> {
                    assertThat(page.getTotalElements()).isEqualTo(0);
                    assertThat(page.getContent()).isEmpty();
                    assertThat(page.getPageable()).isEqualTo(pageable);
                })
                .verifyComplete();

        verify(companyRepositoryPort).count();
        verify(companyRepositoryPort).findAllCompaniesPaged(5L, 5L);
    }
}
