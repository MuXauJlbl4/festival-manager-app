package ru.university.festival.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "participant_types")
public class ParticipantType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean conflictCheckRequired;

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

    public boolean isConflictCheckRequired() {
        return conflictCheckRequired;
    }

    public void setConflictCheckRequired(boolean conflictCheckRequired) {
        this.conflictCheckRequired = conflictCheckRequired;
    }
}
