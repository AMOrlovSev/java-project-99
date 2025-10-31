package hexlet.code.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseEntityTest {

    @Test
    void testBaseEntityIsInterface() {
        assertThat(BaseEntity.class.isInterface()).isTrue();
    }

    @Test
    void testEntitiesImplementBaseEntity() {
        User user = new User();
        Task task = new Task();
        TaskStatus taskStatus = new TaskStatus();
        Label label = new Label();

        assertThat(user).isInstanceOf(BaseEntity.class);
        assertThat(task).isInstanceOf(BaseEntity.class);
        assertThat(taskStatus).isInstanceOf(BaseEntity.class);
        assertThat(label).isInstanceOf(BaseEntity.class);
    }
}
