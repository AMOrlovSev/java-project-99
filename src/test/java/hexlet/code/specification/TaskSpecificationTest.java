package hexlet.code.specification;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class TaskSpecificationTest {

    @Autowired
    private TaskSpecification taskSpecification;

    @Test
    void testBuildSpecificationWithNullParams() {
        TaskParamsDTO params = null;
        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
        assertThat(spec).isEqualTo(Specification.where(null));
    }

    @Test
    void testBuildSpecificationWithEmptyParams() {
        TaskParamsDTO params = new TaskParamsDTO();
        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithTitleCont() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setTitleCont("test");

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithAssigneeId() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setAssigneeId(1L);

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithStatus() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setStatus("to_do");

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithLabelId() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setLabelId(1L);

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithAllParams() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setTitleCont("test");
        params.setAssigneeId(1L);
        params.setStatus("to_do");
        params.setLabelId(1L);

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithPartialParams() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setTitleCont("test");

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }

    @Test
    void testBuildSpecificationWithNullValuesInParams() {
        TaskParamsDTO params = new TaskParamsDTO();
        params.setTitleCont(null);
        params.setAssigneeId(null);
        params.setStatus(null);
        params.setLabelId(null);

        Specification<Task> spec = taskSpecification.build(params);

        assertThat(spec).isNotNull();
    }
}
