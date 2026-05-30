package ru.university.festival.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.university.festival.domain.*;
import ru.university.festival.repository.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventParticipantService {

    private static final Logger log = LoggerFactory.getLogger(EventParticipantService.class);

    private static final int TRANSFER_BUFFER_MINUTES = 30;

    private final EventParticipantRepository eventParticipantRepository;
    private final FestivalEventRepository eventRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantTypeRepository participantTypeRepository;

    public EventParticipantService(
            EventParticipantRepository eventParticipantRepository,
            FestivalEventRepository eventRepository,
            ParticipantRepository participantRepository,
            ParticipantTypeRepository participantTypeRepository
    ) {
        this.eventParticipantRepository = eventParticipantRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.participantTypeRepository = participantTypeRepository;
    }

    @Transactional
    public EventParticipant registerCurrentUserForEvent(Long eventId, AppUser currentUser) {
        log.info("Registering user {} for event {}", currentUser.getUsername(), eventId);
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessRuleException("Мероприятие не найдено."));

        var participant = participantRepository.findByEmail(currentUser.getEmail()).orElseGet(() -> {
            log.info("No participant found for email {}, creating a new one.", currentUser.getEmail());
            var guestType = participantTypeRepository.findByName("Гость")
                    .orElseThrow(() -> new IllegalStateException("Participant type 'Гость' not found."));
            var newParticipant = new Participant(guestType, currentUser.getFullName(), currentUser.getPhone(), currentUser.getEmail());
            return participantRepository.save(newParticipant);
        });

        var existingRegistration = eventParticipantRepository.findByEventIdAndParticipantId(eventId, participant.getId());
        if (existingRegistration.isPresent()) {
            log.warn("User {} is already registered for event {}", currentUser.getUsername(), eventId);
            throw new BusinessRuleException("Вы уже записаны на это мероприятие.");
        }

        var eventParticipant = new EventParticipant(event, participant, "Участник", "CONFIRMED");
        return this.save(eventParticipant);
    }

    @Transactional
    public EventParticipant save(EventParticipant eventParticipant) {
        log.debug("Saving EventParticipant: {}", eventParticipant);
        if (eventParticipant.getId() == null) { // Attach references only for new entities
            attachReferences(eventParticipant);
        }
        validateCapacity(eventParticipant);
        validateParticipantSchedule(eventParticipant);
        log.debug("EventParticipant saved successfully: {}", eventParticipant);
        return eventParticipantRepository.save(eventParticipant);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting EventParticipant with id: {}", id);
        EventParticipant eventParticipant = eventParticipantRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Назначение участника не найдено."));
        var startsAt = LocalDateTime.of(eventParticipant.getEvent().getEventDate(), eventParticipant.getEvent().getStartsAt());
        if (!LocalDateTime.now().isBefore(startsAt)) {
            log.warn("Attempted to delete event participant after event start time.");
            throw new BusinessRuleException("Удалить участника можно только до начала мероприятия.");
        }
        eventParticipantRepository.delete(eventParticipant);
        log.debug("EventParticipant deleted successfully: {}", id);
    }

    public double occupancyPercent(Long eventId) {
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessRuleException("Мероприятие не найдено."));
        long confirmed = eventParticipantRepository.countByEventIdAndStatus(eventId, ParticipationStatus.CONFIRMED);
        return confirmed * 100.0 / event.getRoom().getCapacity();
    }

    private void validateCapacity(EventParticipant eventParticipant) {
        log.debug("Validating capacity for event participant: {}", eventParticipant);
        if (eventParticipant.getStatus() != ParticipationStatus.CONFIRMED) {
            log.debug("Participant status is not CONFIRMED, skipping capacity validation.");
            return;
        }
        long confirmed = eventParticipantRepository.countByEventIdAndStatus(
                eventParticipant.getEvent().getId(),
                ParticipationStatus.CONFIRMED
        );
        // Only check for new registrations, not for updates
        if (eventParticipant.getId() == null) {
            confirmed++;
            log.debug("New participant, incrementing confirmed count to {}.", confirmed);
        }
        if (confirmed > eventParticipant.getEvent().getRoom().getCapacity()) {
            log.warn("Capacity validation failed: confirmed participants {} exceeds room capacity {}.", confirmed, eventParticipant.getEvent().getRoom().getCapacity());
            throw new BusinessRuleException("Количество подтвержденных участников превышает вместимость помещения.");
        }
        log.debug("Capacity validation successful. Confirmed participants: {}.", confirmed);
    }

    private void attachReferences(EventParticipant eventParticipant) {
        log.debug("Attaching references for event participant: {}", eventParticipant);
        Long eventId = eventParticipant.getEvent().getId();
        Long participantId = eventParticipant.getParticipant().getId();
        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Event with id {} not found during reference attachment.", eventId);
                    return new BusinessRuleException("Мероприятие не найдено.");
                });
        var participant = participantRepository.findById(participantId)
                .orElseThrow(() -> {
                    log.warn("Participant with id {} not found during reference attachment.", participantId);
                    return new BusinessRuleException("Участник не найден.");
                });
        eventParticipant.setEvent(event);
        eventParticipant.setParticipant(participant);
        log.debug("References attached successfully.");
    }

    private void validateParticipantSchedule(EventParticipant eventParticipant) {
        log.debug("Validating participant schedule for event participant: {}", eventParticipant);
        if (!eventParticipant.getParticipant().getParticipantType().isConflictCheckRequired()) {
            log.debug("Conflict check not required for participant type, skipping schedule validation.");
            return;
        }
        var event = eventParticipant.getEvent();
        LocalTime startsAt = event.getStartsAt().minusMinutes(TRANSFER_BUFFER_MINUTES);
        LocalTime endsAt = event.getEndsAt().plusMinutes(TRANSFER_BUFFER_MINUTES);
        log.debug("Checking for schedule conflicts for participant {} on date {} between {} and {}.",
                eventParticipant.getParticipant().getId(), event.getEventDate(), startsAt, endsAt);
        var conflicts = eventRepository.findParticipantScheduleConflicts(
                eventParticipant.getParticipant().getId(),
                event.getEventDate(),
                startsAt,
                endsAt,
                event.getId()
        );
        if (!conflicts.isEmpty()) {
            log.warn("Participant schedule conflict detected: {}", conflicts);
            throw new BusinessRuleException("Выступающий уже занят в это время с учетом буфера на перемещение.");
        }
        log.debug("Participant schedule validation successful.");
    }
}
