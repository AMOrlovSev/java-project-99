package hexlet.code.service.impl;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public Page<Task> getAll(Specification<Task> spec, Pageable pageable) {
        return taskRepository.findAll(spec, pageable);
    }

    @Override
    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public Task create(TaskCreateDTO taskData) {
        Task task = taskMapper.map(taskData);
        return taskRepository.save(task);
    }

    @Override
    public Task update(Long id, TaskUpdateDTO taskData) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        taskMapper.update(taskData, taskToUpdate);
        return taskRepository.save(taskToUpdate);
    }

    @Override
    public void delete(Long id) {
        Task taskToDelete = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        User assignee = taskToDelete.getAssignee();
        if (assignee != null) {
            assignee.getAssignedTasks().remove(taskToDelete);
        }

        taskRepository.delete(taskToDelete);
    }
}
