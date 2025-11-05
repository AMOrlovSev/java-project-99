package hexlet.code.controller;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.service.TaskService;
import hexlet.code.specification.TaskSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Задачи", description = "API для управления задачами")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final TaskSpecification taskSpecification;

    @GetMapping("/tasks")
    @Operation(summary = "Получить список всех задач",
            description = "Возвращает список всех задач с возможностью фильтрации по названию, "
                    + "исполнителю, статусу и метке. Поддерживает пагинацию через параметр page.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список задач успешно получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @Parameter(name = "titleCont", description = "Фильтр по названию задачи (содержит подстроку)", example = "create")
    @Parameter(name = "assigneeId", description = "Фильтр по ID исполнителя", example = "1")
    @Parameter(name = "status", description = "Фильтр по слагу статуса", example = "to_be_fixed")
    @Parameter(name = "labelId", description = "Фильтр по ID метки", example = "1")
    @Parameter(name = "page", description = "Номер страницы для пагинации", example = "1")
    public ResponseEntity<List<TaskDTO>> index(
            @ModelAttribute TaskParamsDTO params,
            @RequestParam(defaultValue = "1") int page) {

        var spec = taskSpecification.build(params);
        Page<Task> tasksPage = taskService.getAll(spec, PageRequest.of(page - 1, 10));
        List<TaskDTO> taskDTOs = tasksPage.getContent().stream()
                .map(taskMapper::map)
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasksPage.getTotalElements()))
                .body(taskDTOs);
    }

    @GetMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public TaskDTO show(@PathVariable Long id) {
        var task = taskService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        return taskMapper.map(task);
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать новую задачу", description = "Создает новую задачу с указанными параметрами")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Задача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные задачи"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "404", description = "Статус задачи или исполнитель не найдены")
    })
    public TaskDTO create(@Valid @RequestBody TaskCreateDTO taskData) {
        Task task = taskService.create(taskData);
        return taskMapper.map(task);
    }

    @PutMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Обновить задачу", description = "Обновляет данные существующей задачи")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные задачи"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public TaskDTO update(@RequestBody @Valid TaskUpdateDTO taskData, @PathVariable Long id) {
        Task task = taskService.update(id, taskData);
        return taskMapper.map(task);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить задачу", description = "Удаляет задачу по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}
