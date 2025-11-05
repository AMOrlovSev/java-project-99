package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    Page<Task> getAll(Specification<Task> spec, Pageable pageable);
    List<Task> getAll();
    Optional<Task> findById(Long id);
    Task create(TaskCreateDTO taskData);
    Task update(Long id, TaskUpdateDTO taskData);
    void delete(Long id);
    boolean hasTasksWithUser(Long userId);
    boolean hasTasksWithStatus(Long statusId);
}
