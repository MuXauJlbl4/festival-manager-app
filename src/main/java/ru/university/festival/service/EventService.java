package ru.university.festival.service;

import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.university.festival.domain.EventStatus;
import ru.university.festival.domain.FestivalEvent;
import ru.university.festival.repository.EventParticipantRepository;
import ru.university.festival.repository.FestivalEventRepository;

@Service
public class EventService {

    private final FestivalEventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;

    public EventService(
            FestivalEventRepository eventRepository,
            EventParticipantRepository eventParticipantRepository
    ) {
        this.eventRepository = eventRepository;
        this.eventParticipantRepository = eventParticipantRepository;
    }

    @Transactional
    public FestivalEvent save(FestivalEvent event) {
        validateTime(event);
        validateRoomIsFree(event);
        validateFinishedStatus(event);
        return eventRepository.save(event);
    }

    public Duration duration(FestivalEvent event) {
        return Duration.between(event.getStartsAt(), event.getEndsAt());
    }

    private void validateTime(FestivalEvent event) {
        if (!event.getEndsAt().isAfter(event.getStartsAt())) {
            throw new BusinessRuleException("Время окончания мероприятия должно быть позже времени начала.");
        }
    }

    private void validateRoomIsFree(FestivalEvent event) {
        var conflicts = eventRepository.findRoomScheduleConflicts(
                event.getRoom().getId(),
                event.getEventDate(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getId()
        );
        if (!conflicts.isEmpty()) {
            throw new BusinessRuleException("В выбранном помещении уже есть мероприятие на это время.");
        }
    }

    private void validateFinishedStatus(FestivalEvent event) {
        if (event.getStatus() != EventStatus.FINISHED) {
            return;
        }
        var endsAt = LocalDateTime.of(event.getEventDate(), event.getEndsAt());
        if (endsAt.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Завершить мероприятие можно только после фактического окончания.");
        }
    }

    @Transactional
    public void cancel(Long eventId) {
        FestivalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessRuleException("Мероприятие не найдено."));
        event.setStatus(EventStatus.CANCELLED);
        eventParticipantRepository.findByEventId(eventId).forEach(eventParticipantRepository::delete);
        eventRepository.save(event);
    }
}
