package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.Role;
import ru.university.festival.repository.RoleRepository;

@RestController
@RequestMapping("/api/roles")
public class RoleController extends AbstractCrudController<Role> {
    public RoleController(RoleRepository repository) {
        super(repository);
    }
}
