package ru.university.festival.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.university.festival.domain.AppUser;
import ru.university.festival.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser save(AppUser appUser) {
        if (appUser.getPassword() != null && !appUser.getPassword().startsWith("$2")) {
            appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        }
        return appUserRepository.save(appUser);
    }
}
