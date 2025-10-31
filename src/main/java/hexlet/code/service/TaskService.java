package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Autowired
    private LabelRepository labelRepository;

    public Page<Task> getAll(Specification<Task> spec, Pageable pageable) {
        return taskRepository.findAll(spec, pageable);
    }

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

        if (taskData.getLabelIds() != null) {
            Set<Label> labels = new HashSet<>();
            for (Long labelId : taskData.getLabelIds()) {
                Label label = labelRepository.findById(labelId)
                        .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + labelId));
                labels.add(label);
            }
            task.setLabels(labels);
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
            User oldAssignee = taskToUpdate.getAssignee();
            if (oldAssignee != null) {
                oldAssignee.getAssignedTasks().remove(taskToUpdate);
            }

            if (taskData.getAssigneeId().isPresent() && taskData.getAssigneeId().get() != null) {
                Long newAssigneeId = taskData.getAssigneeId().get();
                User assignee = userRepository.findById(newAssigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + newAssigneeId));
                taskToUpdate.setAssignee(assignee);
                assignee.addAssignedTask(taskToUpdate);
            } else {
                taskToUpdate.setAssignee(null);
            }
        }

        if (taskData.getLabelIds() != null && taskData.getLabelIds().isPresent()) {
            Set<Long> newLabelIds = taskData.getLabelIds().get();
            Set<Label> newLabels = new HashSet<>();

            for (Long labelId : newLabelIds) {
                Label label = labelRepository.findById(labelId)
                        .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + labelId));
                newLabels.add(label);
            }

            taskToUpdate.getLabels().clear();
            taskToUpdate.getLabels().addAll(newLabels);
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
