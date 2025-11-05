package hexlet.code.controller;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.service.LabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Метки", description = "API для управления метками задач")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class LabelController {

    private final LabelService labelService;
    private final LabelMapper labelMapper;

    @GetMapping("/labels")
    @Operation(summary = "Получить список всех меток", description = "Возвращает список всех доступных меток")
    @ApiResponse(responseCode = "200", description = "Список меток успешно получен")
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = labelService.getAll();
        List<LabelDTO> labelDTOs = labels.stream()
                .map(labelMapper::map)
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labelDTOs.size()))
                .body(labelDTOs);
    }

    @GetMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить метку по ID", description = "Возвращает метку по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Метка успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Метка не найдена")
    })
    public LabelDTO show(@PathVariable Long id) {
        var label = labelService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label Not Found: " + id));
        return labelMapper.map(label);
    }

    @PostMapping("/labels")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать новую метку", description = "Создает новую метку с указанным названием")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Метка успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные метки"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "409", description = "Метка с таким названием уже существует")
    })
    public LabelDTO create(@Valid @RequestBody LabelCreateDTO labelData) {
        Label label = labelService.create(labelData);
        return labelMapper.map(label);
    }

    @PutMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Обновить метку", description = "Обновляет данные существующей метки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Метка успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные метки"),
            @ApiResponse(responseCode = "404", description = "Метка не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "409", description = "Метка с таким названием уже существует")
    })
    public LabelDTO update(@RequestBody @Valid LabelUpdateDTO labelData, @PathVariable Long id) {
        Label label = labelService.update(id, labelData);
        return labelMapper.map(label);
    }

    @DeleteMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить метку", description = "Удаляет метку по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Метка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Метка не найдена"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить метку, так как с ней связаны задачи")
    })
    public void delete(@PathVariable Long id) {
        labelService.delete(id);
    }
}
