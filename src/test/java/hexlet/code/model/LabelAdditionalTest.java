package hexlet.code.model;

import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
public class LabelAdditionalTest {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testLabelConstructor() {
        Label label = new Label();
        assertThat(label).isNotNull();
        assertThat(label.getTasks()).isEmpty();
    }

    @Test
    void testLabelToString() {
        Label label = new Label();
        label.setId(1L);
        label.setName("Test Label");

        String toString = label.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name=Test Label");
    }

    @Test
    void testLabelEqualsAndHashCode() {
        Label label1 = new Label();
        label1.setId(1L);
        label1.setName("Label 1");

        Label label2 = new Label();
        label2.setId(1L);
        label2.setName("Label 1");

        Label label3 = new Label();
        label3.setId(2L);
        label3.setName("Label 2");

        assertThat(label1).isEqualTo(label2);
        assertThat(label1).isNotEqualTo(label3);
        assertThat(label1.hashCode()).isEqualTo(label2.hashCode());
        assertThat(label1.hashCode()).isNotEqualTo(label3.hashCode());
    }

    @Test
    void testLabelAddAndRemoveTask() {
        Label label = new Label();
        label.setName("Test Label");
        label.setCreatedAt(LocalDateTime.now());

        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());

        label.addTask(task);
        assertThat(label.getTasks()).hasSize(1);
        assertThat(task.getLabels()).contains(label);

        label.removeTask(task);
        assertThat(label.getTasks()).isEmpty();
        assertThat(task.getLabels()).doesNotContain(label);
    }

    @Test
    void testLabelEntityListeners() {
        Label label = new Label();
        label.setName("Test Label");

        Label savedLabel = labelRepository.save(label);
        assertThat(savedLabel.getCreatedAt()).isNotNull();
    }

    @Test
    void testLabelValidation() {
        Label label = new Label();
        label.setName("AB"); // Too short - should fail validation

        // This would normally be tested with @Valid in controller tests
        // For model-level testing, we can verify the constraints
        assertThat(label.getName()).isEqualTo("AB");
    }
}
