package ru.university.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.Festival;

public interface FestivalRepository extends JpaRepository<Festival, Long> {
}
