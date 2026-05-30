package ru.university.festival.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
