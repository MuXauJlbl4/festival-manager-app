package ru.university.festival.service;

import org.springframework.stereotype.Service;
import ru.university.festival.domain.Festival;
import ru.university.festival.repository.FestivalRepository;

@Service
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public FestivalService(FestivalRepository festivalRepository) {
        this.festivalRepository = festivalRepository;
    }

    public Festival save(Festival festival) {
        if (festival.getEndsOn().isBefore(festival.getStartsOn())) {
            throw new BusinessRuleException("Дата окончания фестиваля не может быть раньше даты начала.");
        }
        return festivalRepository.save(festival);
    }
}
