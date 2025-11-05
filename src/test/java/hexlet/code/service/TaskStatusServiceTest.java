package hexlet.code.service;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceAlreadyExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class TaskStatusServiceTest {

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Test
    void testFindByIdNotFound() {
        Optional<TaskStatus> result = taskStatusService.findById(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void testCreateTaskStatusWithDuplicateName() {
        TaskStatusCreateDTO dto1 = new TaskStatusCreateDTO();
        dto1.setName("Duplicate Status");
        dto1.setSlug("slug1");
        taskStatusService.create(dto1);

        TaskStatusCreateDTO dto2 = new TaskStatusCreateDTO();
        dto2.setName("Duplicate Status");
        dto2.setSlug("slug2");

        assertThatThrownBy(() -> taskStatusService.create(dto2))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void testCreateTaskStatusWithDuplicateSlug() {
        TaskStatusCreateDTO dto1 = new TaskStatusCreateDTO();
        dto1.setName("Status 1");
        dto1.setSlug("duplicate_slug");
        taskStatusService.create(dto1);

        TaskStatusCreateDTO dto2 = new TaskStatusCreateDTO();
        dto2.setName("Status 2");
        dto2.setSlug("duplicate_slug");

        assertThatThrownBy(() -> taskStatusService.create(dto2))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void testUpdateTaskStatusNotFound() {
        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();

        assertThatThrownBy(() -> taskStatusService.update(9999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testDeleteTaskStatusNotFound() {
        assertThatThrownBy(() -> taskStatusService.delete(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}
