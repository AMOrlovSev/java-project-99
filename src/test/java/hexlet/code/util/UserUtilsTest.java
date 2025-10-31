package hexlet.code.util;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class UserUtilsTest {

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetCurrentUserWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        User result = userUtils.getCurrentUser();
        assertThat(result).isNull();
    }

    @Test
    void testGetCurrentUserWhenUserNotFound() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("nonexistent@example.com", "password")
        );
        User result = userUtils.getCurrentUser();
        assertThat(result).isNull();
    }

    @Test
    void testIsAdminWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        boolean result = userUtils.isAdmin();
        assertThat(result).isFalse();
    }

    @Test
    void testIsCurrentUserWhenNotAuthenticated() {
        SecurityContextHolder.clearContext();
        boolean result = userUtils.isCurrentUser(1L);
        assertThat(result).isFalse();
    }

    @Test
    void testIsCurrentUserWithDifferentUser() {
        User user1 = createTestUser("user1@example.com");
        User user2 = createTestUser("user2@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user1.getEmail(), "password")
        );

        boolean result = userUtils.isCurrentUser(user2.getId());
        assertThat(result).isFalse();
    }

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPasswordDigest("password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
