package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.ParticipantType;
import ru.university.festival.repository.ParticipantTypeRepository;

@RestController
@RequestMapping("/api/participant-types")
public class ParticipantTypeController extends AbstractCrudController<ParticipantType> {
    public ParticipantTypeController(ParticipantTypeRepository repository) {
        super(repository);
    }
}
