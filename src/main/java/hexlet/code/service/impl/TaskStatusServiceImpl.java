package hexlet.code.service.impl;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceAlreadyExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.TaskStatusService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskStatusServiceImpl implements TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;

    @Override
    public List<TaskStatus> getAll() {
        return taskStatusRepository.findAll();
    }

    @Override
    public Optional<TaskStatus> findById(Long id) {
        return taskStatusRepository.findById(id);
    }

    @Override
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

    @Override
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

    @Override
    public void delete(Long id) {
        TaskStatus taskStatusToDelete = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + id));

        if (!taskStatusToDelete.getTasks().isEmpty()) {
            throw new ResourceAlreadyExistsException("Cannot delete task status with associated tasks");
        }

        taskStatusRepository.delete(taskStatusToDelete);
    }
}
