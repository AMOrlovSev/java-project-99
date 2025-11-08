package hexlet.code.service.impl;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
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
        TaskStatus taskStatus = taskStatusMapper.map(taskStatusData);
        return taskStatusRepository.save(taskStatus);
    }

    @Override
    public TaskStatus update(Long id, TaskStatusUpdateDTO taskStatusData) {
        TaskStatus taskStatusToUpdate = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + id));

        taskStatusMapper.update(taskStatusData, taskStatusToUpdate);
        return taskStatusRepository.save(taskStatusToUpdate);
    }

    @Override
    public void delete(Long id) {
        TaskStatus taskStatusToDelete = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + id));

        taskStatusRepository.delete(taskStatusToDelete);
    }
}
