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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
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
            assignee.addAssignedTask(task);
        }

        return taskRepository.save(task);
    }

    public Task update(Long id, TaskUpdateDTO taskData) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        // Обработка статуса
        if (taskData.getStatus() != null && taskData.getStatus().isPresent()) {
            String newStatusSlug = taskData.getStatus().get();
            TaskStatus status = taskStatusRepository.findBySlug(newStatusSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + newStatusSlug));
            taskToUpdate.setTaskStatus(status);
        }

        // Упрощенная обработка исполнителя
        if (taskData.getAssigneeId() != null) {
            // Убираем задачу из списка старого исполнителя
            User oldAssignee = taskToUpdate.getAssignee();
            if (oldAssignee != null) {
                oldAssignee.getAssignedTasks().remove(taskToUpdate);
            }

            // Если передали конкретного исполнителя - назначаем
            if (taskData.getAssigneeId().isPresent() && taskData.getAssigneeId().get() != null) {
                Long newAssigneeId = taskData.getAssigneeId().get();
                User assignee = userRepository.findById(newAssigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + newAssigneeId));
                taskToUpdate.setAssignee(assignee);
                assignee.addAssignedTask(taskToUpdate);
            } else {
                // Если передали null или undefined - снимаем назначение
                taskToUpdate.setAssignee(null);
            }
        }

        taskMapper.update(taskData, taskToUpdate);
        return taskRepository.save(taskToUpdate);
    }

    public void delete(Long id) {
        Task taskToDelete = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));

        User assignee = taskToDelete.getAssignee();
        if (assignee != null) {
            assignee.getAssignedTasks().remove(taskToDelete);
        }

        taskRepository.deleteById(id);
    }

    public boolean hasTasksWithUser(Long userId) {
        return taskRepository.existsTasksByUser(userId);
    }

    public boolean hasTasksWithStatus(Long statusId) {
        return taskRepository.existsTasksByStatus(statusId);
    }
}
