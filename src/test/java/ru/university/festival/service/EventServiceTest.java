package ru.university.festival.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.university.festival.domain.EventStatus;
import ru.university.festival.domain.FestivalEvent;
import ru.university.festival.domain.Room;
import ru.university.festival.repository.EventParticipantRepository;
import ru.university.festival.repository.FestivalEventRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private FestivalEventRepository eventRepository;

    @Mock
    private EventParticipantRepository eventParticipantRepository;

    @InjectMocks
    private EventService eventService;

    private FestivalEvent event;

    @BeforeEach
    void setUp() {
        event = new FestivalEvent();
        event.setId(1L);
        event.setRoom(new Room());
        event.getRoom().setId(1L);
        event.setEventDate(LocalDate.now().plusDays(1));
        event.setStartsAt(LocalTime.of(12, 0));
        event.setEndsAt(LocalTime.of(13, 30));
    }

    @Test
    void durationReturnsDifferenceBetweenStartAndEndTime() {
        assertEquals(90, eventService.duration(event).toMinutes());
    }

    @Test
    void saveRejectsInvalidTimeRange() {
        event.setEndsAt(LocalTime.of(11, 0));
        assertThrows(BusinessRuleException.class, () -> eventService.save(event));
    }

    @Test
    void save_throwsException_whenRoomConflict() {
        when(eventRepository.findRoomScheduleConflicts(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(new FestivalEvent()));
        assertThrows(BusinessRuleException.class, () -> eventService.save(event));
    }

    @Test
    void save_successfully_whenNoRoomConflict() {
        when(eventRepository.findRoomScheduleConflicts(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(eventRepository.save(any(FestivalEvent.class))).thenReturn(event);
        FestivalEvent saved = eventService.save(event);
        assertEquals(event, saved);
    }

    @Test
    void save_throwsException_whenFinishedStatusForFutureEvent() {
        event.setStatus(EventStatus.FINISHED);
        assertThrows(BusinessRuleException.class, () -> eventService.save(event));
    }

    @Test
    void save_successfully_whenFinishedStatusForPastEvent() {
        event.setEventDate(LocalDate.now().minusDays(1));
        event.setStatus(EventStatus.FINISHED);
        when(eventRepository.save(any(FestivalEvent.class))).thenReturn(event);
        FestivalEvent saved = eventService.save(event);
        assertEquals(event, saved);
    }

    @Test
    void cancel_successfully() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        eventService.cancel(1L);
        assertEquals(EventStatus.CANCELLED, event.getStatus());
        verify(eventParticipantRepository, times(1)).findByEventId(1L);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void cancel_throwsException_whenEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessRuleException.class, () -> eventService.cancel(1L));
    }
}
