package hexlet.code.handler;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        ResponseEntity<String> response = exceptionHandler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Resource not found");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithUniqueConstraint() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Exception rootCause = new Exception("unique constraint \"label_name_key\"");

        when(ex.getRootCause()).thenReturn(rootCause);

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Label with this name already exists");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithForeignKeyConstraint() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Exception rootCause = new Exception("foreign key constraint \"task_status_id_fk\"");

        when(ex.getRootCause()).thenReturn(rootCause);

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Cannot delete task status because "
                + "there are tasks associated with it");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithGenericMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Data integrity violation");

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Cannot perform operation due to data integrity constraints");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithNullRootCause() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Test violation");

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Cannot perform operation due to data integrity constraints");
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<String> response = exceptionHandler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo("Access denied: Access denied");
    }

    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<String> response = exceptionHandler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Invalid credentials");
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<String> response = exceptionHandler.handleOtherExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Something went wrong. Please try again later.");
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity response = exceptionHandler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithEmptyErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity response = exceptionHandler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);
    }

    @Test
    void testHandleMethodArgumentNotValidExceptionWithMultipleErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError("user", "email", "Email is required");
        FieldError error2 = new FieldError("user", "password", "Password must be at least 6 characters");
        FieldError error3 = new FieldError("user", "firstName", "First name is required");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error1, error2, error3));

        ResponseEntity response = exceptionHandler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);

        @SuppressWarnings("unchecked")
        java.util.Map<String, String> errors = (java.util.Map<String, String>) response.getBody();
        assertThat(errors).hasSize(3);
        assertThat(errors).containsKeys("email", "password", "firstName");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithUserEmailConstraint() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Exception rootCause = new Exception("unique constraint \"user_email_key\"");

        when(ex.getRootCause()).thenReturn(rootCause);

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("User with this email already exists");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithTaskStatusSlugConstraint() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Exception rootCause = new Exception("unique constraint \"task_status_slug_key\"");

        when(ex.getRootCause()).thenReturn(rootCause);

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Task status with this slug already exists");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithAssigneeConstraint() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Exception rootCause = new Exception("foreign key constraint \"assignee_id_fk\"");

        when(ex.getRootCause()).thenReturn(rootCause);

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Cannot delete user because there are tasks assigned to this user");
    }

    @Test
    void testHandleDataIntegrityViolationExceptionWithLabelConstraint() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Exception rootCause = new Exception("foreign key constraint \"task_labels_label_id_fk\"");

        when(ex.getRootCause()).thenReturn(rootCause);

        ResponseEntity<String> response = exceptionHandler.handleDataIntegrityViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("Cannot delete label because there are tasks using this label");
    }
}
