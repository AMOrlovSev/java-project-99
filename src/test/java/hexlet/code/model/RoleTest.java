package hexlet.code.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class RoleTest {

    @Test
    void testRoleValues() {
        assertThat(Role.values()).hasSize(2);
        assertThat(Role.values()).containsExactly(Role.ADMIN, Role.USER);
    }

    @Test
    void testRoleValueOf() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
    }

    @Test
    void testRoleNames() {
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
        assertThat(Role.USER.name()).isEqualTo("USER");
    }
}
