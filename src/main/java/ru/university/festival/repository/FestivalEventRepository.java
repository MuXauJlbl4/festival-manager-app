package ru.university.festival.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.university.festival.domain.FestivalEvent;

public interface FestivalEventRepository extends JpaRepository<FestivalEvent, Long> {

    @Query("""
            select e from FestivalEvent e
            where e.room.id = :roomId
              and e.eventDate = :eventDate
              and (:eventId is null or e.id <> :eventId)
              and e.startsAt < :endsAt
              and e.endsAt > :startsAt
            """)
    List<FestivalEvent> findRoomScheduleConflicts(
            @Param("roomId") Long roomId,
            @Param("eventDate") LocalDate eventDate,
            @Param("startsAt") LocalTime startsAt,
            @Param("endsAt") LocalTime endsAt,
            @Param("eventId") Long eventId
    );

    @Query("""
            select e from FestivalEvent e
            join EventParticipant ep on ep.event.id = e.id
            where ep.participant.id = :participantId
              and e.eventDate = :eventDate
              and (:eventId is null or e.id <> :eventId)
              and e.startsAt < :endsAt
              and e.endsAt > :startsAt
            """)
    List<FestivalEvent> findParticipantScheduleConflicts(
            @Param("participantId") Long participantId,
            @Param("eventDate") LocalDate eventDate,
            @Param("startsAt") LocalTime startsAt,
            @Param("endsAt") LocalTime endsAt,
            @Param("eventId") Long eventId
    );
}
