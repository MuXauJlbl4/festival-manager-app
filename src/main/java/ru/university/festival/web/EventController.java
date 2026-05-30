package ru.university.festival.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.university.festival.domain.AppUser;
import ru.university.festival.domain.EventParticipant;
import ru.university.festival.domain.FestivalEvent;
import ru.university.festival.repository.FestivalEventRepository;
import ru.university.festival.service.EventParticipantService;
import ru.university.festival.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController extends AbstractCrudController<FestivalEvent> {

    private final EventService eventService;
    private final EventParticipantService eventParticipantService;

    public EventController(
            FestivalEventRepository repository,
            EventService eventService,
            EventParticipantService eventParticipantService
    ) {
        super(repository);
        this.eventService = eventService;
        this.eventParticipantService = eventParticipantService;
    }

    @Override
    protected FestivalEvent save(FestivalEvent entity) {
        return eventService.save(entity);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        eventService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventParticipant> register(@PathVariable Long id, @AuthenticationPrincipal AppUser user) {
        EventParticipant registration = eventParticipantService.registerCurrentUserForEvent(id, user);
        return ResponseEntity.ok(registration);
    }
}
