package ru.university.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
