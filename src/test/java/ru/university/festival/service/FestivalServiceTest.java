package ru.university.festival.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import ru.university.festival.domain.Festival;
import ru.university.festival.repository.FestivalRepository;

class FestivalServiceTest {

    private final FestivalService festivalService = new FestivalService(mock(FestivalRepository.class));

    @Test
    void saveRejectsFestivalWithEndDateBeforeStartDate() {
        var festival = new Festival();
        festival.setName("Городской фестиваль");
        festival.setCity("Ярославль");
        festival.setStartsOn(LocalDate.of(2026, 6, 10));
        festival.setEndsOn(LocalDate.of(2026, 6, 9));

        assertThrows(BusinessRuleException.class, () -> festivalService.save(festival));
    }
}
