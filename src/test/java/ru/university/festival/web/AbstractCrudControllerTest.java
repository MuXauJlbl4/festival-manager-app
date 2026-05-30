package ru.university.festival.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractCrudControllerTest {

    @Mock
    private JpaRepository<TestEntity, Long> repository;

    private TestController controller;
    private TestEntity entity;

    @BeforeEach
    void setUp() {
        controller = new TestController(repository);
        entity = new TestEntity();
        entity.setId(1L);
    }

    @Test
    void findAll_shouldReturnAllEntities() {
        when(repository.findAll()).thenReturn(Collections.singletonList(entity));

        List<TestEntity> result = controller.findAll();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void findById_shouldReturnEntity_whenFound() {
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        TestEntity result = controller.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrowException_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            controller.findById(1L);
        });
    }

    @Test
    void create_shouldReturnCreatedEntity() {
        when(repository.save(any(TestEntity.class))).thenReturn(entity);

        TestEntity result = controller.create(entity);

        assertEquals(1L, result.getId());
    }

    @Test
    void update_shouldReturnUpdatedEntity() {
        when(repository.save(any(TestEntity.class))).thenReturn(entity);

        TestEntity result = controller.update(1L, entity);

        assertEquals(1L, result.getId());
    }

    @Test
    void delete_shouldReturnNoContent_whenSuccessful() {
        when(repository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> result = controller.delete(1L);

        assertEquals(ResponseEntity.noContent().build(), result);
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenNotFound() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            controller.delete(1L);
        });
    }

    private static class TestEntity {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    private static class TestController extends AbstractCrudController<TestEntity> {
        protected TestController(JpaRepository<TestEntity, Long> repository) {
            super(repository);
        }
    }
}
