package hexlet.code.model;

import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class TaskAdditionalTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    private TaskStatus testStatus;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();

        // Создаем обязательный TaskStatus
        testStatus = new TaskStatus();
        testStatus.setName("Test Status");
        testStatus.setSlug("test_status");
        testStatus.setCreatedAt(LocalDateTime.now());
        testStatus = taskStatusRepository.save(testStatus);
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

    private Task createValidTask(String name, String description, User assignee) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(testStatus);
        if (assignee != null) {
            task.setAssignee(assignee);
        }
        return taskRepository.save(task);
    }

    private Label createValidLabel(String name) {
        Label label = new Label();
        label.setName(name);
        label.setCreatedAt(LocalDateTime.now());
        return label;
    }

    @Test
    void testTaskConstructor() {
        Task task = new Task();
        assertThat(task).isNotNull();
        assertThat(task.getLabels()).isEmpty();
    }

    @Test
    void testTaskToString() {
        Task task = createValidTask("Test Task", "Test Description", null);

        String toString = task.toString();
        assertThat(toString).contains("id=" + task.getId());
        assertThat(toString).contains("name=Test Task");
    }

    @Test
    void testTaskEqualsAndHashCode() {
        Task task1 = createValidTask("Task 1", "Description 1", null);
        Task task2 = taskRepository.findById(task1.getId()).orElseThrow();
        Task task3 = createValidTask("Task 2", "Description 2", null);

        assertThat(task1).isEqualTo(task2);
        assertThat(task1).isNotEqualTo(task3);
        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
        assertThat(task1.hashCode()).isNotEqualTo(task3.hashCode());
    }

    @Test
    void testTaskAddAndRemoveLabel() {
        Task task = createValidTask("Test Task", "Test Description", null);

        Label label = createValidLabel("Test Label");

        task.addLabel(label);
        assertThat(task.getLabels()).hasSize(1);
        assertThat(label.getTasks()).contains(task);

        task.removeLabel(label);
        assertThat(task.getLabels()).isEmpty();
        assertThat(label.getTasks()).doesNotContain(task);
    }

    @Test
    void testTaskWithMultipleLabels() {
        Task task = createValidTask("Test Task", "Test Description", null);

        Label label1 = createValidLabel("Label 1");
        Label label2 = createValidLabel("Label 2");

        task.setLabels(new HashSet<>());
        task.addLabel(label1);
        task.addLabel(label2);

        assertThat(task.getLabels()).hasSize(1);
        assertThat(task.getLabels()).contains(label1, label2);
    }

    @Test
    void testTaskEntityListeners() {
        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setTaskStatus(testStatus);

        Task savedTask = taskRepository.save(task);
        assertThat(savedTask.getCreatedAt()).isNotNull();
    }

    @Test
    void testTaskValidation() {
        Task task = new Task();
        task.setName("T");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setTaskStatus(testStatus);

        Task savedTask = taskRepository.save(task);
        assertThat(savedTask.getName()).isEqualTo("T");
    }

    @Test
    void testTaskWithNullAssignee() {
        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(testStatus);
        task.setAssignee(null);

        Task savedTask = taskRepository.save(task);
        assertThat(savedTask.getAssignee()).isNull();
    }

    @Test
    void testTaskWithAssignee() {
        User user = createValidUser("test@example.com");
        Task task = createValidTask("Test Task", "Test Description", user);

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(savedTask.getAssignee()).isNotNull();
        assertThat(savedTask.getAssignee().getEmail()).isEqualTo("test@example.com");
    }
}
