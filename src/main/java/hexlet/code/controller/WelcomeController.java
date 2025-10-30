package hexlet.code.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Система", description = "Системные endpoints")
public class WelcomeController {

    @GetMapping(path = "/welcome")
    @Operation(summary = "Приветствие", description = "Возвращает приветственное сообщение для проверки работы API")
    @ApiResponse(responseCode = "200", description = "Приветственное сообщение успешно возвращено")
    public String welcome() {
        return "Welcome to Spring";
    }
}
