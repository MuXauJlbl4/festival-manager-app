package ru.university.festival.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.university.festival.domain.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
