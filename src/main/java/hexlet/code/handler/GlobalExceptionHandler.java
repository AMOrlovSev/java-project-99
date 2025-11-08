package hexlet.code.handler;

import hexlet.code.exception.ResourceNotFoundException;
import io.sentry.Sentry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        Sentry.captureMessage("Resource not found: " + ex.getMessage(), io.sentry.SentryLevel.WARNING);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Sentry.captureMessage("Validation error: " + errors, io.sentry.SentryLevel.WARNING);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Sentry.captureException(ex);

        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        String userMessage;
        if (rootMessage != null) {
            if (rootMessage.contains("unique constraint") || rootMessage.contains("Unique index")
                    || rootMessage.contains("Duplicate entry")) {
                if (rootMessage.contains("label_name") || rootMessage.contains("name")
                        && rootMessage.contains("label")) {
                    userMessage = "Label with this name already exists";
                } else if (rootMessage.contains("task_status_name") || rootMessage.contains("name")
                        && rootMessage.contains("task_status")) {
                    userMessage = "Task status with this name already exists";
                } else if (rootMessage.contains("task_status_slug") || rootMessage.contains("slug")
                        && rootMessage.contains("task_status")) {
                    userMessage = "Task status with this slug already exists";
                } else if (rootMessage.contains("user_email") || rootMessage.contains("email")
                        && rootMessage.contains("user")) {
                    userMessage = "User with this email already exists";
                } else {
                    userMessage = "Resource with this value already exists";
                }
            } else if (rootMessage.contains("foreign key constraint")) {
                if (rootMessage.contains("task_status")) {
                    userMessage = "Cannot delete task status because there are tasks associated with it";
                } else if (rootMessage.contains("assignee")) {
                    userMessage = "Cannot delete user because there are tasks assigned to this user";
                } else if (rootMessage.contains("label")) {
                    userMessage = "Cannot delete label because there are tasks using this label";
                } else {
                    userMessage = "Cannot perform operation due to existing relationships";
                }
            } else {
                userMessage = "Cannot perform operation due to data integrity constraints";
            }
        } else {
            userMessage = "Cannot perform operation due to data integrity constraints";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(userMessage);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        Sentry.captureMessage("Access denied: " + ex.getMessage(), io.sentry.SentryLevel.WARNING);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        Sentry.captureMessage("Bad credentials attempt", io.sentry.SentryLevel.WARNING);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOtherExceptions(Exception ex) {
        Sentry.captureException(ex);
        String message = "Something went wrong. Please try again later.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
