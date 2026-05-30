package ru.university.festival.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.university.festival.domain.*;
import ru.university.festival.repository.EventParticipantRepository;
import ru.university.festival.repository.FestivalEventRepository;
import ru.university.festival.repository.ParticipantRepository;

@ExtendWith(MockitoExtension.class)
class EventParticipantServiceTest {

    @Mock
    private EventParticipantRepository eventParticipantRepository;
    @Mock
    private FestivalEventRepository festivalEventRepository;
    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private EventParticipantService eventParticipantService;

    private FestivalEvent mockEvent;
    private Participant mockParticipant;
    private Room mockRoom;
    private ParticipantType mockParticipantType;

    @BeforeEach
    void setUp() {
        mockRoom = new Room();
        mockRoom.setId(1L);
        mockRoom.setCapacity(100);

        mockEvent = new FestivalEvent();
        mockEvent.setId(10L);
        mockEvent.setRoom(mockRoom);
        mockEvent.setEventDate(LocalDate.now());
        mockEvent.setStartsAt(LocalTime.of(10, 0));
        mockEvent.setEndsAt(LocalTime.of(11, 0));

        mockParticipantType = new ParticipantType();
        mockParticipantType.setId(100L);
        mockParticipantType.setName("Speaker");
        mockParticipantType.setConflictCheckRequired(true);

        mockParticipant = new Participant();
        mockParticipant.setId(1000L);
        mockParticipant.setParticipantType(mockParticipantType);
    }

    @Test
    void occupancyPercentUsesConfirmedParticipantsAndRoomCapacity() {
        when(festivalEventRepository.findById(10L)).thenReturn(Optional.of(mockEvent));
        when(eventParticipantRepository.countByEventIdAndStatus(10L, ParticipationStatus.CONFIRMED)).thenReturn(25L);

        assertEquals(25.0, eventParticipantService.occupancyPercent(10L));
    }
}
