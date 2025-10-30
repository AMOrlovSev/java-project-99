package hexlet.code.service;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceAlreadyExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskStatusService {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private TaskService taskService;

    public List<TaskStatus> getAll() {
        return taskStatusRepository.findAll();
    }

    public Optional<TaskStatus> findById(Long id) {
        return taskStatusRepository.findById(id);
    }

    public TaskStatus create(TaskStatusCreateDTO taskStatusData) {
        if (taskStatusRepository.existsByName(taskStatusData.getName())) {
            throw new ResourceAlreadyExistsException("Task status with name "
                    + taskStatusData.getName() + " already exists");
        }
        if (taskStatusRepository.existsBySlug(taskStatusData.getSlug())) {
            throw new ResourceAlreadyExistsException("Task status with slug "
                    + taskStatusData.getSlug() + " already exists");
        }

        TaskStatus taskStatus = taskStatusMapper.map(taskStatusData);
        return taskStatusRepository.save(taskStatus);
    }

    public TaskStatus update(Long id, TaskStatusUpdateDTO taskStatusData) {
        TaskStatus taskStatusToUpdate = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + id));

        if (taskStatusData.getName() != null && taskStatusData.getName().isPresent()) {
            String newName = taskStatusData.getName().get();
            if (!newName.equals(taskStatusToUpdate.getName()) && taskStatusRepository.existsByName(newName)) {
                throw new ResourceAlreadyExistsException("Task status with name " + newName + " already exists");
            }
        }

        if (taskStatusData.getSlug() != null && taskStatusData.getSlug().isPresent()) {
            String newSlug = taskStatusData.getSlug().get();
            if (!newSlug.equals(taskStatusToUpdate.getSlug()) && taskStatusRepository.existsBySlug(newSlug)) {
                throw new ResourceAlreadyExistsException("Task status with slug " + newSlug + " already exists");
            }
        }

        taskStatusMapper.update(taskStatusData, taskStatusToUpdate);
        return taskStatusRepository.save(taskStatusToUpdate);
    }

    public void delete(Long id) {
        TaskStatus taskStatusToDelete = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + id));

        if (taskService.hasTasksWithStatus(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete task status with associated tasks");
        }

        taskStatusRepository.deleteById(id);
    }
}
