package hexlet.code.model;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class UserAdditionalTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserConstructor() {
        User user = new User();
        assertThat(user).isNotNull();
        assertThat(user.getAssignedTasks()).isEmpty();
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void testUserToString() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        String toString = user.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("email=test@example.com");
    }

    @Test
    void testUserEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("user1@example.com");

        User user3 = new User();
        user3.setId(2L);
        user3.setEmail("user2@example.com");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1).isNotEqualTo(user3);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1.hashCode()).isNotEqualTo(user3.hashCode());
    }

    @Test
    void testUserAddAndRemoveAssignedTask() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordDigest("password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        Task task = new Task();
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setIndex(1);
        task.setCreatedAt(LocalDateTime.now());

        user.addAssignedTask(task);
        assertThat(user.getAssignedTasks()).hasSize(1);
        assertThat(task.getAssignee()).isEqualTo(user);

        user.removeAssignedTask(task);
        assertThat(user.getAssignedTasks()).isEmpty();
        assertThat(task.getAssignee()).isNull();
    }

    @Test
    void testUserEntityListeners() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordDigest("password");

        User savedUser = userRepository.save(user);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void testUserDetailsMethods() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest("encodedPassword");
        user.setRole(Role.ADMIN);

        assertThat(user.getUsername()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("encodedPassword");

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");

        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void testUserWithUserRole() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordDigest("password");
        user.setRole(Role.USER);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void testUserWithMultipleAssignedTasks() {
        User user = new User();
        user.setEmail("multi@example.com");
        user.setFirstName("Multi");
        user.setLastName("Task");
        user.setPasswordDigest("password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

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

        user.addAssignedTask(task1);
        user.addAssignedTask(task2);

        assertThat(user.getAssignedTasks()).hasSize(2);
        assertThat(user.getAssignedTasks()).contains(task1, task2);
    }

    @Test
    void testUserValidation() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setPasswordDigest("");

        assertThat(user.getEmail()).isEqualTo("invalid-email");
        assertThat(user.getPasswordDigest()).isEmpty();
    }
}
