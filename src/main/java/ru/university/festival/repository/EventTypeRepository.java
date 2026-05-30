package ru.university.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.EventType;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {
    Optional<EventType> findByName(String name);
}
