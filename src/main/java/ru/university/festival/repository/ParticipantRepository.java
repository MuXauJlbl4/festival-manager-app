package ru.university.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.Participant;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByEmail(String email);
}
