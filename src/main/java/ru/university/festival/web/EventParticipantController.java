package ru.university.festival.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ru.university.festival.domain.EventParticipant;
import ru.university.festival.repository.EventParticipantRepository;
import ru.university.festival.service.EventParticipantService;

@RestController
@RequestMapping("/api/event-participants")
public class EventParticipantController extends AbstractCrudController<EventParticipant> {

    private final EventParticipantService eventParticipantService;

    public EventParticipantController(
            EventParticipantRepository repository,
            EventParticipantService eventParticipantService
    ) {
        super(repository);
        this.eventParticipantService = eventParticipantService;
    }

    @Override
    protected EventParticipant save(EventParticipant entity) {
        return eventParticipantService.save(entity);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventParticipantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{eventId}/occupancy")
    public OccupancyResponse occupancy(@PathVariable Long eventId) {
        return new OccupancyResponse(eventId, eventParticipantService.occupancyPercent(eventId));
    }
}
