package ru.university.festival.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import java.lang.reflect.Method;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public abstract class AbstractCrudController<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractCrudController.class);

    private final JpaRepository<T, Long> repository;

    protected AbstractCrudController(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<T> findAll() {
        log.debug("Fetching all entities.");
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public T findById(@PathVariable Long id) {
        log.debug("Fetching entity by id: {}", id);
        return repository.findById(id).orElseThrow(() -> {
            log.warn("Entity with id {} not found.", id);
            return new ResourceNotFoundException("Запись не найдена: " + id);
        });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public T create(@Valid @RequestBody T entity) {
        log.debug("Creating new entity: {}", entity);
        return save(entity);
    }

    @PutMapping("/{id}")
    public T update(@PathVariable Long id, @Valid @RequestBody T entity) {
        log.debug("Updating entity with id: {}: {}", id, entity);
        setId(entity, id);
        return save(entity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("Deleting entity with id: {}", id);
        if (!repository.existsById(id)) {
            log.warn("Attempted to delete non-existent entity with id: {}", id);
            throw new ResourceNotFoundException("Запись не найдена: " + id);
        }
        repository.deleteById(id);
        log.debug("Entity with id {} deleted successfully.", id);
        return ResponseEntity.noContent().build();
    }

    protected T save(T entity) {
        log.debug("Saving entity: {}", entity);
        return repository.save(entity);
    }

    private void setId(T entity, Long id) {
        log.debug("Setting id {} for entity: {}", id, entity);
        try {
            Method method = entity.getClass().getMethod("setId", Long.class);
            method.invoke(entity, id);
        } catch (ReflectiveOperationException exception) {
            log.error("Failed to set ID {} for entity {}: {}", id, entity, exception.getMessage());
            throw new IllegalStateException("Entity must have setId(Long).", exception);
        }
    }
}
