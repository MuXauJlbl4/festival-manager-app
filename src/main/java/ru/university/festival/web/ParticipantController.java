package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.Participant;
import ru.university.festival.repository.ParticipantRepository;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController extends AbstractCrudController<Participant> {
    public ParticipantController(ParticipantRepository repository) {
        super(repository);
    }
}
