package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.Festival;
import ru.university.festival.repository.FestivalRepository;
import ru.university.festival.service.FestivalService;

@RestController
@RequestMapping("/api/festivals")
public class FestivalController extends AbstractCrudController<Festival> {

    private final FestivalService festivalService;

    public FestivalController(FestivalRepository repository, FestivalService festivalService) {
        super(repository);
        this.festivalService = festivalService;
    }

    @Override
    protected Festival save(Festival entity) {
        return festivalService.save(entity);
    }
}
