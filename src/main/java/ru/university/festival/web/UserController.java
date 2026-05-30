package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.AppUser;
import ru.university.festival.repository.AppUserRepository;
import ru.university.festival.service.AppUserService;

@RestController
@RequestMapping("/api/users")
public class UserController extends AbstractCrudController<AppUser> {

    private final AppUserService appUserService;

    public UserController(AppUserRepository repository, AppUserService appUserService) {
        super(repository);
        this.appUserService = appUserService;
    }

    @Override
    protected AppUser save(AppUser entity) {
        return appUserService.save(entity);
    }
}
