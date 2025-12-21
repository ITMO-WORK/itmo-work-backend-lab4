package org.ilestegor.applicationservice.adapter.output.feign.vacancy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VacancyFeignAdapter {

    private final VacancyClient vacancyClient;


}
