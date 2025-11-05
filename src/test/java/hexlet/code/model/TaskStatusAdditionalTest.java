package hexlet.code.model;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class TaskStatusAdditionalTest {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testTaskStatusConstructor() {
        TaskStatus taskStatus = new TaskStatus();
        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getTasks()).isEmpty();
    }

    @Test
    void testTaskStatusToString() {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setId(1L);
        taskStatus.setName("Test Status");
        taskStatus.setSlug("test_status");

        String toString = taskStatus.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Test Status");
        assertThat(toString).contains("slug=test_status");
    }

    @Test
    void testTaskStatusEqualsAndHashCode() {
        TaskStatus status1 = new TaskStatus();
        status1.setId(1L);
        status1.setName("Status 1");
        status1.setSlug("status_1");

        TaskStatus status2 = new TaskStatus();
        status2.setId(1L);
        status2.setName("Status 1");
        status2.setSlug("status_1");

        TaskStatus status3 = new TaskStatus();
        status3.setId(2L);
        status3.setName("Status 2");
        status3.setSlug("status_2");

        assertThat(status1).isEqualTo(status2);
        assertThat(status1).isNotEqualTo(status3);
        assertThat(status1.hashCode()).isEqualTo(status2.hashCode());
        assertThat(status1.hashCode()).isNotEqualTo(status3.hashCode());
    }

    @Test
    void testTaskStatusAddAndRemoveTask() {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Test Status");
        taskStatus.setSlug("test_status");
        taskStatus.setCreatedAt(LocalDateTime.now());

        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());

        taskStatus.addTask(task);
        assertThat(taskStatus.getTasks()).hasSize(1);
        assertThat(task.getTaskStatus()).isEqualTo(taskStatus);

        taskStatus.removeTask(task);
        assertThat(taskStatus.getTasks()).isEmpty();
        assertThat(task.getTaskStatus()).isNull();
    }

    @Test
    void testTaskStatusEntityListeners() {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Test Status");
        taskStatus.setSlug("test_status");

        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);
        assertThat(savedStatus.getCreatedAt()).isNotNull();
    }

    @Test
    void testTaskStatusValidation() {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("S");
        taskStatus.setSlug("s");

        assertThat(taskStatus.getName()).isEqualTo("S");
        assertThat(taskStatus.getSlug()).isEqualTo("s");
    }

    @Test
    void testTaskStatusUniqueConstraints() {
        TaskStatus status1 = new TaskStatus();
        status1.setName("Unique Status");
        status1.setSlug("unique_status");
        taskStatusRepository.save(status1);

        TaskStatus status2 = new TaskStatus();
        status2.setName("Unique Status");
        status2.setSlug("different_slug");

        assertThat(status1).isNotEqualTo(status2);
    }

    @Test
    void testTaskStatusWithMultipleTasks() {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Multi Task Status");
        taskStatus.setSlug("multi_status");
        taskStatus.setCreatedAt(LocalDateTime.now());

        Task task1 = new Task();
        task1.setName("Task 1");
        task1.setDescription("Description 1");
        task1.setIndex(1);
        task1.setCreatedAt(LocalDateTime.now());

        Task task2 = new Task();
        task2.setName("Task 2");
        task2.setDescription("Description 2");
        task2.setIndex(2);
        task2.setCreatedAt(LocalDateTime.now());

        taskStatus.addTask(task1);
        taskStatus.addTask(task2);

        assertThat(taskStatus.getTasks()).hasSize(2);
        assertThat(taskStatus.getTasks()).contains(task1, task2);
    }
}
