package ru.university.festival.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.university.festival.domain.AppUser;
import ru.university.festival.repository.AppUserRepository;
import ru.university.festival.repository.RoleRepository;
import ru.university.festival.service.AppUserService;

@Configuration
public class InitialDataLoader {

    @Bean
    CommandLineRunner commandLineRunner(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            AppUserService appUserService
    ) {
        return args -> {
            // Create Users for all roles
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "admin", "admin", "ADMIN", "Администратор системы", "admin@festival.local", "79990000000");
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "organizer", "organizer", "ORGANIZER", "Организатор фестиваля", "organizer@festival.local", "79990000001");
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "manager", "manager", "MANAGER", "Менеджер мероприятия", "manager@festival.local", "79990000002");
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "user", "user", "USER", "Обычный пользователь", "user@festival.local", "79990000003");
        };
    }

    private AppUser createUserIfMissing(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            AppUserService appUserService,
            String username,
            String password,
            String roleName,
            String fullName,
            String email,
            String phone
    ) {
        return appUserRepository.findByUsername(username).orElseGet(() -> {
            var role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException(roleName + " role was not created by Liquibase."));
            var user = new AppUser();
            user.setRole(role);
            user.setFullName(fullName);
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setPhone(phone);
            user.setStatus("ACTIVE");
            return appUserService.save(user);
        });
    }
}
