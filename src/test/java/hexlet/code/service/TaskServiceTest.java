package hexlet.code.service;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class TaskServiceTest {

    @Autowired
    private TaskServiceImpl taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    private User testUser;
    private TaskStatus testStatus;
    private Label testLabel;

    @BeforeEach
    void setUp() {
        testUser = createUser("task-service@example.com", "Task", "Service");
        testStatus = createTaskStatus("Service Status", "service_status");
        testLabel = createLabel("service_label");
    }

    private User createUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordDigest("password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private TaskStatus createTaskStatus(String name, String slug) {
        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        status.setCreatedAt(LocalDateTime.now());
        return taskStatusRepository.save(status);
    }

    private Label createLabel(String name) {
        Label label = new Label();
        label.setName(name);
        label.setCreatedAt(LocalDateTime.now());
        return labelRepository.save(label);
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

    @Test
    void testCreateTaskWithNullStatus() {
        TaskCreateDTO taskData = new TaskCreateDTO();
        taskData.setTitle("Test Task");
        taskData.setStatus(null);

        assertThatThrownBy(() -> taskService.create(taskData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task status is required");
    }

    @Test
    void testCreateTaskWithBlankStatus() {
        TaskCreateDTO taskData = new TaskCreateDTO();
        taskData.setTitle("Test Task");
        taskData.setStatus("");

        assertThatThrownBy(() -> taskService.create(taskData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task status is required");
    }

    @Test
    void testCreateTaskWithInvalidAssignee() {
        TaskCreateDTO taskData = new TaskCreateDTO();
        taskData.setTitle("Test Task");
        taskData.setStatus(testStatus.getSlug());
        taskData.setAssigneeId(9999L);

        assertThatThrownBy(() -> taskService.create(taskData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testCreateTaskWithInvalidLabel() {
        TaskCreateDTO taskData = new TaskCreateDTO();
        taskData.setTitle("Test Task");
        taskData.setStatus(testStatus.getSlug());
        taskData.setTaskLabelIds(Set.of(9999L));

        assertThatThrownBy(() -> taskService.create(taskData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Label not found");
    }

    @Test
    void testUpdateTaskWithNullAssignee() {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Original Task");
        createDTO.setStatus(testStatus.getSlug());
        createDTO.setAssigneeId(testUser.getId());

        var createdTask = taskService.create(createDTO);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssigneeId(JsonNullable.of(null));

        var updatedTask = taskService.update(createdTask.getId(), updateDTO);

        assertThat(updatedTask.getAssignee()).isNull();
    }

    @Test
    void testUpdateTaskWithEmptyLabels() {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Task with Labels");
        createDTO.setStatus(testStatus.getSlug());
        createDTO.setTaskLabelIds(Set.of(testLabel.getId()));

        var createdTask = taskService.create(createDTO);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTaskLabelIds(JsonNullable.of(Set.of()));

        var updatedTask = taskService.update(createdTask.getId(), updateDTO);

        assertThat(updatedTask.getLabels()).isEmpty();
    }

    @Test
    void testUpdateTaskWithNewLabels() {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Task without Labels");
        createDTO.setStatus(testStatus.getSlug());

        var createdTask = taskService.create(createDTO);

        Label newLabel = createLabel("new_label");

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTaskLabelIds(JsonNullable.of(Set.of(newLabel.getId())));

        var updatedTask = taskService.update(createdTask.getId(), updateDTO);

        assertThat(updatedTask.getLabels()).hasSize(1);
        assertThat(updatedTask.getLabels()).contains(newLabel);
    }

    @Test
    void testUpdateTaskStatusToInvalid() {
        var task = taskService.create(new TaskCreateDTO() {{
            setTitle("Test Task");
            setStatus(testStatus.getSlug());
        }});

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setStatus(JsonNullable.of("invalid_status"));

        assertThatThrownBy(() -> taskService.update(task.getId(), updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task status not found");
    }

    @Test
    void testUpdateTaskAssigneeToInvalid() {
        var task = taskService.create(new TaskCreateDTO() {{
            setTitle("Test Task");
            setStatus(testStatus.getSlug());
        }});

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssigneeId(JsonNullable.of(9999L));

        assertThatThrownBy(() -> taskService.update(task.getId(), updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testUpdateTaskWithInvalidLabel() {
        var task = taskService.create(new TaskCreateDTO() {{
            setTitle("Test Task");
            setStatus(testStatus.getSlug());
        }});

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTaskLabelIds(JsonNullable.of(Set.of(9999L)));

        assertThatThrownBy(() -> taskService.update(task.getId(), updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Label not found");
    }

    @Test
    void testGetAllTasksWithoutSpecification() {
        taskService.create(new TaskCreateDTO() {{
            setTitle("Task 1");
            setStatus(testStatus.getSlug());
        }});

        taskService.create(new TaskCreateDTO() {{
            setTitle("Task 2");
            setStatus(testStatus.getSlug());
        }});

        var result = taskService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name")
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    void testFindExistingTask() {
        var createdTask = taskService.create(new TaskCreateDTO() {{
            setTitle("Findable Task");
            setStatus(testStatus.getSlug());
        }});

        Optional<Task> foundTask = taskService.findById(createdTask.getId());

        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getName()).isEqualTo("Findable Task");
    }
}
