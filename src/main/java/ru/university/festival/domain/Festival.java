package ru.university.festival.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "festivals")
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotNull
    private LocalDate startsOn;

    @NotNull
    private LocalDate endsOn;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FestivalStatus status = FestivalStatus.PLANNED;

    public Festival() {
    }

    public Festival(String name, String city, LocalDate startsOn, LocalDate endsOn, String status) {
        this.name = name;
        this.city = city;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
        this.status = FestivalStatus.valueOf(status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public void setStartsOn(LocalDate startsOn) {
        this.startsOn = startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }

    public void setEndsOn(LocalDate endsOn) {
        this.endsOn = endsOn;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public FestivalStatus getStatus() {
        return status;
    }

    public void setStatus(FestivalStatus status) {
        this.status = status;
    }
}
