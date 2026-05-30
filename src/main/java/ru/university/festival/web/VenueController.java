package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.Venue;
import ru.university.festival.repository.VenueRepository;

@RestController
@RequestMapping("/api/venues")
public class VenueController extends AbstractCrudController<Venue> {
    public VenueController(VenueRepository repository) {
        super(repository);
    }
}
