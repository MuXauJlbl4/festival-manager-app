package ru.university.festival.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.EventParticipant;
import ru.university.festival.domain.ParticipationStatus;

import java.util.Optional;

public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {
    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    List<EventParticipant> findByEventId(Long eventId);

    Optional<EventParticipant> findByEventIdAndParticipantId(Long eventId, Long participantId);

    List<EventParticipant> findByParticipant_Email(String email);
}
