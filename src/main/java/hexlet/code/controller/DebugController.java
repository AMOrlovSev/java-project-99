package hexlet.code.controller;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@Tag(name = "Отладка", description = "Endpoints для отладки и мониторинга")
public class DebugController {

    @GetMapping("/sentry-test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Тест Sentry", description = "Генерирует тестовое исключение для проверки Sentry")
    @ApiResponse(responseCode = "200", description = "Тест выполнен")
    public String testSentry() {
        try {
            throw new RuntimeException("This is a test exception for Sentry");
        } catch (Exception e) {
             Sentry.captureException(e);
            return "Test exception generated (Sentry disabled)";
        }
    }

    @GetMapping("/sentry-message")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Тест сообщения Sentry", description = "Отправляет тестовое сообщение в Sentry")
    @ApiResponse(responseCode = "200", description = "Тест выполнен")
    public String testSentryMessage() {
         Sentry.captureMessage("Test message from Task Manager API", io.sentry.SentryLevel.INFO);
        return "Test message (Sentry disabled)";
    }
}
