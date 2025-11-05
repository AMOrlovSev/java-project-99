package hexlet.code.service;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskStatusService {
    List<TaskStatus> getAll();
    Optional<TaskStatus> findById(Long id);
    TaskStatus create(TaskStatusCreateDTO taskStatusData);
    TaskStatus update(Long id, TaskStatusUpdateDTO taskStatusData);
    void delete(Long id);
}
