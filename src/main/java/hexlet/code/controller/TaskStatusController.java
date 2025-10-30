package hexlet.code.controller;

import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Статусы задач", description = "API для управления статусами задач")
@SecurityRequirement(name = "bearerAuth")
public class TaskStatusController {

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @GetMapping("/task_statuses")
    @Operation(summary = "Получить список всех статусов задач",
            description = "Возвращает список всех доступных статусов задач")
    @ApiResponse(responseCode = "200", description = "Список статусов успешно получен")
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var taskStatuses = taskStatusService.getAll();
        List<TaskStatusDTO> taskStatusDTOs = taskStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(taskStatusDTOs.size()))
                .body(taskStatusDTOs);
    }

    @GetMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить статус задачи по ID",
            description = "Возвращает статус задачи по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус задачи успешно найден"),
            @ApiResponse(responseCode = "404", description = "Статус задачи не найден")
    })
    public TaskStatusDTO show(@PathVariable Long id) {
        var taskStatus = taskStatusService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Status Not Found: " + id));
        return taskStatusMapper.map(taskStatus);
    }

    @PostMapping("/task_statuses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Создать новый статус задачи",
            description = "Создает новый статус задачи с указанными параметрами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Статус задачи успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные статуса"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "409", description = "Статус с таким именем или slug уже существует")
    })
    public TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO taskStatusData) {
        TaskStatus taskStatus = taskStatusService.create(taskStatusData);
        return taskStatusMapper.map(taskStatus);
    }

    @PutMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Обновить статус задачи", description = "Обновляет данные существующего статуса задачи")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус задачи успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные статуса"),
            @ApiResponse(responseCode = "404", description = "Статус задачи не найден"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "409", description = "Статус с таким именем или slug уже существует")
    })
    public TaskStatusDTO update(@RequestBody @Valid TaskStatusUpdateDTO taskStatusData, @PathVariable Long id) {
        TaskStatus taskStatus = taskStatusService.update(id, taskStatusData);
        return taskStatusMapper.map(taskStatus);
    }

    @DeleteMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Удалить статус задачи", description = "Удаляет статус задачи по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Статус задачи успешно удален"),
            @ApiResponse(responseCode = "404", description = "Статус задачи не найден"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить статус, так как с ним связаны задачи")
    })
    public void delete(@PathVariable Long id) {
        taskStatusService.delete(id);
    }
}
