package hexlet.code.service;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class TaskServiceAdditionalTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @BeforeEach
    void setUp() {
    }

    private User createValidUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordDigest("valid-password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private TaskStatus createValidTaskStatus(String name, String slug) {
        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        status.setCreatedAt(LocalDateTime.now());
        return taskStatusRepository.save(status);
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Task> result = taskService.findById(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void testGetAllWithSpecification() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        User user = createValidUser("test@example.com");
        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        task.setAssignee(user);
        taskRepository.save(task);

        Specification<Task> spec = Specification.where(null);
        Page<Task> result = taskService.getAll(spec, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Task");
    }

    @Test
    void testGetAllWithoutPagination() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        taskRepository.save(task);

        var result = taskService.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Task");
    }

    @Test
    void testCreateTaskWithInvalidStatus() {
        TaskCreateDTO taskData = new TaskCreateDTO();
        taskData.setTitle("Test Task");
        taskData.setStatus("invalid_status");

        assertThatThrownBy(() -> taskService.create(taskData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task status not found");
    }

    @Test
    void testUpdateTaskNotFound() {
        TaskUpdateDTO updateData = new TaskUpdateDTO();

        assertThatThrownBy(() -> taskService.update(9999L, updateData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    void testDeleteTaskNotFound() {
        assertThatThrownBy(() -> taskService.delete(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }
}
