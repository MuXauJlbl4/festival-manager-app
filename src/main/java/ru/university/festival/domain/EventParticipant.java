package ru.university.festival.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_participants", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "participant_id"}))
public class EventParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private FestivalEvent event;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @NotBlank
    @Size(max = 100)
    private String roleAtEvent;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ParticipationStatus status = ParticipationStatus.INVITED;

    private LocalDateTime registrationTime;

    public EventParticipant() {
    }

    public EventParticipant(FestivalEvent event, Participant participant, String roleAtEvent, String status) {
        this.event = event;
        this.participant = participant;
        this.roleAtEvent = roleAtEvent;
        this.status = ParticipationStatus.valueOf(status);
        this.registrationTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FestivalEvent getEvent() {
        return event;
    }

    public void setEvent(FestivalEvent event) {
        this.event = event;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public String getRoleAtEvent() {
        return roleAtEvent;
    }

    public void setRoleAtEvent(String roleAtEvent) {
        this.roleAtEvent = roleAtEvent;
    }

    public ParticipationStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }
}
