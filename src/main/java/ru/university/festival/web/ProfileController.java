package ru.university.festival.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.AppUser;
import ru.university.festival.domain.EventParticipant;
import ru.university.festival.repository.EventParticipantRepository;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final EventParticipantRepository eventParticipantRepository;

    public ProfileController(EventParticipantRepository eventParticipantRepository) {
        this.eventParticipantRepository = eventParticipantRepository;
    }

    @GetMapping("/my-events")
    public ResponseEntity<List<EventParticipant>> getMyEvents(@AuthenticationPrincipal AppUser user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<EventParticipant> registrations = eventParticipantRepository.findByParticipant_Email(user.getEmail());
        return ResponseEntity.ok(registrations);
    }
}
