package ru.university.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.ParticipantType;

import java.util.Optional;

public interface ParticipantTypeRepository extends JpaRepository<ParticipantType, Long> {
    Optional<ParticipantType> findByName(String name);
}
