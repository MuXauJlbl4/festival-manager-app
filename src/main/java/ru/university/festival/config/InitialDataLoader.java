package ru.university.festival.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.university.festival.domain.*;
import ru.university.festival.repository.*;
import ru.university.festival.service.AppUserService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class InitialDataLoader {

    @Bean
    CommandLineRunner commandLineRunner(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            AppUserService appUserService,
            FestivalRepository festivalRepository,
            VenueRepository venueRepository,
            RoomRepository roomRepository,
            EventTypeRepository eventTypeRepository,
            ParticipantTypeRepository participantTypeRepository,
            ParticipantRepository participantRepository,
            FestivalEventRepository festivalEventRepository,
            EventParticipantRepository eventParticipantRepository
    ) {
        return args -> {
            // Create Users
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "admin", "admin", "ADMIN", "Администратор системы", "admin@festival.local", "79990000000");
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "organizer", "organizer", "ORGANIZER", "Организатор фестиваля", "organizer@festival.local", "79990000001");
            var manager = createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "manager", "manager", "MANAGER", "Менеджер мероприятия", "manager@festival.local", "79990000002");
            createUserIfMissing(appUserRepository, roleRepository, appUserService,
                    "user", "user", "USER", "Обычный пользователь", "user@festival.local", "79990000003");

            // Seed sample data only if the database is empty
            if (festivalRepository.count() == 0) {
                // Dictionaries
                var speakerType = participantTypeRepository.findByName("Выступающий").orElseThrow();
                var staffType = participantTypeRepository.findByName("Персонал").orElseThrow();
                var concertType = eventTypeRepository.findByName("Концерт").orElseThrow();
                var lectureType = eventTypeRepository.findByName("Лекция").orElseThrow();

                // Festivals
                var musicFest = new Festival("Летний Музыкальный Фестиваль", "Москва", LocalDate.parse("2026-07-15"), LocalDate.parse("2026-07-17"), "PLANNED");
                var techConf = new Festival("Техно-Конференция 2026", "Санкт-Петербург", LocalDate.parse("2026-09-20"), LocalDate.parse("2026-09-22"), "PLANNED");
                festivalRepository.saveAll(List.of(musicFest, techConf));

                // Venues & Rooms
                var parkVenue = new Venue(musicFest, "Парк 'Искусств'", "Москва", "ул. Центральная, 1", "ACTIVE", "");
                venueRepository.save(parkVenue);
                var mainStage = new Room(parkVenue, "Главная Сцена", 5000, 1, "");
                var smallStage = new Room(parkVenue, "Малая Сцена", 1000, 1, "");
                roomRepository.saveAll(List.of(mainStage, smallStage));

                var confVenue = new Venue(techConf, "Конференц-центр 'Прогресс'", "Санкт-Петербург", "пр. Науки, 15", "ACTIVE", "");
                venueRepository.save(confVenue);
                var bigHall = new Room(confVenue, "Зал 'Большой'", 800, 2, "Проектор, звук");
                var roundTable = new Room(confVenue, "Зал 'Круглый стол'", 50, 3, "Экран");
                roomRepository.saveAll(List.of(bigHall, roundTable));

                // Participants
                var p1 = new Participant(speakerType, "Группа 'Космические Мелодии'", "79991000001", "melodies@space.com");
                var p2 = new Participant(speakerType, "DJ Электрон'", "79991000002", "electron@techno.com");
                var p3 = new Participant(speakerType, "Анна Петрова (Эксперт по AI)", "79991000003", "anna.p@ai-research.com");
                var p4 = new Participant(speakerType, "Иван Сидоров (Разработчик квантовых систем)", "79991000004", "ivan.s@quantum.dev");
                var p5 = new Participant(staffType, "Служба безопасности 'Щит'", "79991000005", "security@shield.org");
                participantRepository.saveAll(List.of(p1, p2, p3, p4, p5));

                // Events
                var e1 = new FestivalEvent(musicFest, mainStage, concertType, manager, "Открытие фестиваля: Космические Мелодии", LocalDate.parse("2026-07-15"), LocalTime.parse("19:00:00"), LocalTime.parse("21:00:00"), "PLANNED");
                var e2 = new FestivalEvent(musicFest, smallStage, concertType, manager, "Ночной сет от DJ Электрона", LocalDate.parse("2026-07-15"), LocalTime.parse("22:00:00"), LocalTime.parse("23:59:00"), "PLANNED");
                var e3 = new FestivalEvent(techConf, bigHall, lectureType, manager, "Лекция: Будущее искусственного интеллекта", LocalDate.parse("2026-09-21"), LocalTime.parse("10:00:00"), LocalTime.parse("11:30:00"), "PLANNED");
                var e4 = new FestivalEvent(techConf, roundTable, lectureType, manager, "Мастер-класс: Первые шаги в квантовых вычислениях", LocalDate.parse("2026-09-21"), LocalTime.parse("12:00:00"), LocalTime.parse("14:00:00"), "PLANNED");
                festivalEventRepository.saveAll(List.of(e1, e2, e3, e4));

                // Event-Participant Assignments
                eventParticipantRepository.save(new EventParticipant(e1, p1, "Хедлайнер", "CONFIRMED"));
                eventParticipantRepository.save(new EventParticipant(e2, p2, "DJ", "CONFIRMED"));
                eventParticipantRepository.save(new EventParticipant(e3, p3, "Спикер", "CONFIRMED"));
                eventParticipantRepository.save(new EventParticipant(e4, p4, "Ведущий", "CONFIRMED"));
            }
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
