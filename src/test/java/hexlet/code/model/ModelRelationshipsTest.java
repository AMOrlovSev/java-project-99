package hexlet.code.model;

import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
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
        user.setPasswordDigest("valid-password"); // Обязательное поле
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
        Label label = createValidLabel("Test Label");
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Task task = createValidTask("Test Task", "Test Description", status, null);

        label.addTask(task);
        labelRepository.save(label);

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();

        Set<Label> labels = savedTask.getLabels();
        assertThat(labels).hasSize(1);
        assertThat(labels.iterator().next().getName()).isEqualTo("Test Label");
    }

    @Test
    void testTaskLabelRelationships() {
        TaskStatus status = createValidTaskStatus("Test Status", "test_status");
        Task task = createValidTask("Test Task", "Test Description", status, null);

        Label label1 = createValidLabel("Label 1");
        Label label2 = createValidLabel("Label 2");

        task.addLabel(label1);
        task.addLabel(label2);
        taskRepository.save(task);

        Task savedTask = taskRepository.findById(task.getId()).orElseThrow();
        Set<Label> labels = savedTask.getLabels();
        assertThat(labels).hasSize(2);

        boolean hasLabel1 = labels.stream().anyMatch(l -> l.getName().equals("Label 1"));
        boolean hasLabel2 = labels.stream().anyMatch(l -> l.getName().equals("Label 2"));
        assertThat(hasLabel1).isTrue();
        assertThat(hasLabel2).isTrue();

        task.removeLabel(label1);
        taskRepository.save(task);

        Task finalTask = taskRepository.findById(task.getId()).orElseThrow();
        Set<Label> finalLabels = finalTask.getLabels();
        assertThat(finalLabels).hasSize(1);
        assertThat(finalLabels.iterator().next().getName()).isEqualTo("Label 2");
    }

    @Test
    void testUserDetailsMethods() {
        User user = createValidUser("test@example.com");
        user.setRole(Role.USER);
        userRepository.save(user);

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
}
