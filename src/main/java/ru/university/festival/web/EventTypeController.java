package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.EventType;
import ru.university.festival.repository.EventTypeRepository;

@RestController
@RequestMapping("/api/event-types")
public class EventTypeController extends AbstractCrudController<EventType> {
    public EventTypeController(EventTypeRepository repository) {
        super(repository);
    }
}
