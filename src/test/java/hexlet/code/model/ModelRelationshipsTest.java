package hexlet.code.model;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class ModelRelationshipsTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

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

    private Task createValidTask(String name, String description, TaskStatus status, User assignee) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        if (assignee != null) {
            task.setAssignee(assignee);
        }
        return taskRepository.save(task);
    }

    private Label createValidLabel(String name) {
        Label label = new Label();
        label.setName(name);
        return labelRepository.save(label);
    }

    @Test
    void testTaskStatusRelationships() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Task task = createValidTask("Test Task", "Test Description", status, null);

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(savedTask.getTaskStatus()).isEqualTo(status);
        assertThat(savedTask.getTaskStatus().getName()).isEqualTo("Test Status");
    }

    @Test
    void testUserRelationships() {
        User user = createValidUser("test@example.com");
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Task task = createValidTask("Test Task", "Test Description", status, user);

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(savedTask.getAssignee()).isEqualTo(user);
        assertThat(savedTask.getAssignee().getEmail()).isEqualTo("test@example.com");

        User savedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("valid-password");
    }

    @Test
    void testLabelRelationships() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Task task = createValidTask("Test Task", "Test Description", status, null);
        Label label = createValidLabel("Test Label");

        Task taskWithLabel = new Task();
        taskWithLabel.setName("Task With Label");
        taskWithLabel.setDescription("Description");
        taskWithLabel.setIndex(2);
        taskWithLabel.setCreatedAt(LocalDateTime.now());
        taskWithLabel.setTaskStatus(status);
        taskWithLabel.addLabel(label);

        Task savedTaskWithLabel = taskRepository.save(taskWithLabel);

        assertThat(savedTaskWithLabel.getLabels()).contains(label);
    }

    @Test
    void testTaskLabelRelationships() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");

        Label label1 = createValidLabel("Label 1");
        Label label2 = createValidLabel("Label 2");

        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        task.addLabel(label1);
        task.addLabel(label2);

        Task savedTask = taskRepository.save(task);

        Set<Label> labels = savedTask.getLabels();
        assertThat(labels).hasSize(2);
        assertThat(labels).contains(label1, label2);
    }

    @Test
    void testUserDetailsMethods() {
        User user = createValidUser("test@example.com");
        user.setRole(Role.USER);

        assertThat(user.getUsername()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("valid-password");
        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void testBaseEntityMarker() {
        User user = new User();
        Task task = new Task();
        TaskStatus status = new TaskStatus();
        Label label = new Label();

        assertThat(user).isInstanceOf(BaseEntity.class);
        assertThat(task).isInstanceOf(BaseEntity.class);
        assertThat(status).isInstanceOf(BaseEntity.class);
        assertThat(label).isInstanceOf(BaseEntity.class);
    }

    @Test
    void testEntityEqualsAndHashCode() {
        User user1 = createValidUser("test1@example.com");
        User user2 = createValidUser("test2@example.com");

        assertThat(user1).isNotEqualTo(user2);
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());

        assertThat(user1).isEqualTo(user1);

        assertThat(user1).isNotEqualTo(null);

        assertThat(user1).isNotEqualTo("string");
    }

    @Test
    void testBidirectionalRelationships() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        User user = createValidUser("test@example.com");
        Label label = createValidLabel("Test Label");

        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        task.setAssignee(user);
        task.addLabel(label);

        Task savedTask = taskRepository.save(task);

        assertThat(savedTask.getTaskStatus()).isEqualTo(status);
        assertThat(savedTask.getAssignee()).isEqualTo(user);
        assertThat(savedTask.getLabels()).contains(label);
    }

    @Test
    void testManyToManyRelationshipOperations() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Label label1 = createValidLabel("Label 1");
        Label label2 = createValidLabel("Label 2");
        Label label3 = createValidLabel("Label 3");

        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        task.addLabel(label1);
        task.addLabel(label2);

        Task savedTask = taskRepository.save(task);
        assertThat(savedTask.getLabels()).hasSize(2);

        Task taskWithThreeLabels = new Task();
        taskWithThreeLabels.setName("Task With Three Labels");
        taskWithThreeLabels.setDescription("Description");
        taskWithThreeLabels.setIndex(2);
        taskWithThreeLabels.setCreatedAt(LocalDateTime.now());
        taskWithThreeLabels.setTaskStatus(status);
        taskWithThreeLabels.addLabel(label1);
        taskWithThreeLabels.addLabel(label2);
        taskWithThreeLabels.addLabel(label3);

        Task savedTaskWithThreeLabels = taskRepository.save(taskWithThreeLabels);
        assertThat(savedTaskWithThreeLabels.getLabels()).hasSize(3);
    }

    @Test
    void testTaskCreationWithRelationships() {
        User user = createValidUser("creator@example.com");
        TaskStatus status = createValidTaskStatus("In Progress", "in_progress");
        Label bugLabel = createValidLabel("bug");
        Label featureLabel = createValidLabel("feature");

        Task task = new Task();
        task.setName("Complex Task");
        task.setDescription("Task with all relationships");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());
        task.setTaskStatus(status);
        task.setAssignee(user);
        task.addLabel(bugLabel);
        task.addLabel(featureLabel);

        Task savedTask = taskRepository.save(task);

        assertThat(savedTask.getTaskStatus()).isEqualTo(status);
        assertThat(savedTask.getAssignee()).isEqualTo(user);
        assertThat(savedTask.getLabels()).hasSize(2);
        assertThat(savedTask.getLabels()).contains(bugLabel, featureLabel);
    }

    @Test
    void testEntityCreationAndBasicProperties() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordDigest("password");
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }
}
