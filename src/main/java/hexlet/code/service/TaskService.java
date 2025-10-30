package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Task create(TaskCreateDTO taskData) {
        if (taskData.getStatus() == null || taskData.getStatus().isBlank()) {
            throw new ResourceNotFoundException("Task status is required");
        }

        Task task = taskMapper.map(taskData);

        TaskStatus status = taskStatusRepository.findBySlug(taskData.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + taskData.getStatus()));
        task.setTaskStatus(status);

        if (taskData.getAssigneeId() != null) {
            User assignee = userRepository.findById(taskData.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + taskData.getAssigneeId()));
            task.setAssignee(assignee);
        }

        return taskRepository.save(task);
    }

    public Task update(Long id, TaskUpdateDTO taskData) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        if (taskData.getStatus() != null && taskData.getStatus().isPresent()) {
            String newStatusSlug = taskData.getStatus().get();
            TaskStatus status = taskStatusRepository.findBySlug(newStatusSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + newStatusSlug));
            taskToUpdate.setTaskStatus(status);
        }

        if (taskData.getAssigneeId() != null) {
            if (taskData.getAssigneeId().isPresent()) {
                Long newAssigneeId = taskData.getAssigneeId().get();
                // Если newAssigneeId не null - находим пользователя
                if (newAssigneeId != null) {
                    User assignee = userRepository.findById(newAssigneeId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + newAssigneeId));
                    taskToUpdate.setAssignee(assignee);
                } else {
                    // Если явно передали null - снимаем назначение
                    taskToUpdate.setAssignee(null);
                }
            }
            // Если assigneeId.isPresent() = false (JsonNullable.undefined) - ничего не делаем
        }

        // Применяем остальные обновления через маппер (только простые поля)
        taskMapper.update(taskData, taskToUpdate);
        return taskRepository.save(taskToUpdate);
    }

    public void delete(Long id) {
        Task taskToDelete = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
        taskRepository.deleteById(id);
    }

    public boolean hasTasksWithUser(Long userId) {
        return taskRepository.existsTasksByUser(userId);
    }

    public boolean hasTasksWithStatus(Long statusId) {
        return taskRepository.existsTasksByStatus(statusId);
    }
}
