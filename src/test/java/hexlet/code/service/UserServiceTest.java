package hexlet.code.service;

import hexlet.code.DatabaseCleanerExtension;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ExtendWith(DatabaseCleanerExtension.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testFindByIdNotFound() {
        Optional<User> result = userService.findById(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void testCreateUserWithPasswordEncoding() {
        UserCreateDTO userData = new UserCreateDTO();
        userData.setEmail("test@example.com");
        userData.setFirstName("Test");
        userData.setLastName("User");
        userData.setPassword("plainPassword");

        User user = userService.create(userData);

        assertThat(passwordEncoder.matches("plainPassword", user.getPasswordDigest())).isTrue();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getFirstName()).isEqualTo("Test");
        assertThat(user.getLastName()).isEqualTo("User");
    }

    @Test
    void testCreateUserWithDuplicateEmail() {
        UserCreateDTO userData1 = new UserCreateDTO();
        userData1.setEmail("duplicate@example.com");
        userData1.setFirstName("First");
        userData1.setLastName("User");
        userData1.setPassword("password");
        userService.create(userData1);

        UserCreateDTO userData2 = new UserCreateDTO();
        userData2.setEmail("duplicate@example.com");
        userData2.setFirstName("Second");
        userData2.setLastName("User");
        userData2.setPassword("password");

        assertThatThrownBy(() -> userService.create(userData2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testUpdateUserPassword() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("update@example.com");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");
        createDTO.setPassword("oldPassword");

        User user = userService.create(createDTO);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setPassword(JsonNullable.of("newPassword"));

        User updatedUser = userService.update(user.getId(), updateDTO);

        assertThat(passwordEncoder.matches("newPassword", updatedUser.getPasswordDigest())).isTrue();
        assertThat(passwordEncoder.matches("oldPassword", updatedUser.getPasswordDigest())).isFalse();
    }

    @Test
    void testUpdateUserEmail() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("original@example.com");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");
        createDTO.setPassword("password");

        User user = userService.create(createDTO);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of("updated@example.com"));

        User updatedUser = userService.update(user.getId(), updateDTO);

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void testUpdateUserWithDuplicateEmail() {
        UserCreateDTO userData1 = new UserCreateDTO();
        userData1.setEmail("user1@example.com");
        userData1.setFirstName("First");
        userData1.setLastName("User");
        userData1.setPassword("password");
        User user1 = userService.create(userData1);

        UserCreateDTO userData2 = new UserCreateDTO();
        userData2.setEmail("user2@example.com");
        userData2.setFirstName("Second");
        userData2.setLastName("User");
        userData2.setPassword("password");
        User user2 = userService.create(userData2);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of("user1@example.com"));

        assertThatThrownBy(() -> userService.update(user2.getId(), updateDTO))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testUpdateUserNotFound() {
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        assertThatThrownBy(() -> userService.update(9999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testDeleteUserNotFound() {
        assertThatThrownBy(() -> userService.delete(9999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void testUpdateUserWithoutPasswordChange() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("nopasswordchange@example.com");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");
        createDTO.setPassword("originalPassword");

        User user = userService.create(createDTO);
        String originalPasswordHash = user.getPasswordDigest();

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("UpdatedName"));

        User updatedUser = userService.update(user.getId(), updateDTO);

        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedName");
        assertThat(updatedUser.getPasswordDigest()).isEqualTo(originalPasswordHash);
        assertThat(passwordEncoder.matches("originalPassword", updatedUser.getPasswordDigest())).isTrue();
    }

    @Test
    void testUpdateUserPartialData() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("partial@example.com");
        createDTO.setFirstName("Original");
        createDTO.setLastName("User");
        createDTO.setPassword("password");

        User user = userService.create(createDTO);

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setLastName(JsonNullable.of("UpdatedLastName"));

        User updatedUser = userService.update(user.getId(), updateDTO);

        assertThat(updatedUser.getFirstName()).isEqualTo("Original");
        assertThat(updatedUser.getLastName()).isEqualTo("UpdatedLastName");
        assertThat(updatedUser.getEmail()).isEqualTo("partial@example.com");
    }

    @Test
    void testGetAllUsers() {
        UserCreateDTO user1 = new UserCreateDTO();
        user1.setEmail("user1@test.com");
        user1.setFirstName("User1");
        user1.setLastName("Test");
        user1.setPassword("password");
        userService.create(user1);

        UserCreateDTO user2 = new UserCreateDTO();
        user2.setEmail("user2@test.com");
        user2.setFirstName("User2");
        user2.setLastName("Test");
        user2.setPassword("password");
        userService.create(user2);

        var result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("email")
                .containsExactlyInAnyOrder("user1@test.com", "user2@test.com");
    }
}
