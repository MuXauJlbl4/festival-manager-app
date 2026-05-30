package ru.university.festival.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.university.festival.domain.Room;
import ru.university.festival.repository.RoomRepository;

@RestController
@RequestMapping("/api/rooms")
public class RoomController extends AbstractCrudController<Room> {
    public RoomController(RoomRepository repository) {
        super(repository);
    }
}
