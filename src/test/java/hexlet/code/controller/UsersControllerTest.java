package hexlet.code.controller;

import hexlet.code.DatabaseCleanerExtension;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.Role;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(DatabaseCleanerExtension.class)
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    public void setUp() {
        createTestUsers();
    }

    private void createTestUsers() {
        testUser = createUser("test@example.com", "Test", "User", "password123", Role.USER);
        adminUser = createUser("admin@example.com", "Admin", "User", "admin123", Role.ADMIN);
    }

    private User createUser(String email, String firstName, String lastName, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordDigest(password);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testIndex() throws Exception {
        var response = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        List<UserDTO> actual = objectMapper.readValue(body, new TypeReference<>() {

        });
        List<User> expectedUsers = userRepository.findAll();

        Set<Long> actualIds = actual.stream().map(UserDTO::getId).collect(Collectors.toSet());
        Set<Long> expectedIds = expectedUsers.stream().map(User::getId).collect(Collectors.toSet());
        Assertions.assertThat(actualIds).isEqualTo(expectedIds);

        Set<String> actualEmails = actual.stream().map(UserDTO::getEmail).collect(Collectors.toSet());
        Set<String> expectedEmails = expectedUsers.stream().map(User::getEmail).collect(Collectors.toSet());
        Assertions.assertThat(actualEmails).isEqualTo(expectedEmails);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testShow() throws Exception {
        var response = mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        UserDTO actual = objectMapper.readValue(body, UserDTO.class);
        User expectedUser = userRepository.findById(testUser.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedUser.getId());
        Assertions.assertThat(actual.getEmail()).isEqualTo(expectedUser.getEmail());
        Assertions.assertThat(actual.getFirstName()).isEqualTo(expectedUser.getFirstName());
        Assertions.assertThat(actual.getLastName()).isEqualTo(expectedUser.getLastName());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    public void testCreateUserWithoutToken() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("New");
        userCreateDTO.setLastName("User");
        userCreateDTO.setEmail("newuser@example.com");
        userCreateDTO.setPassword("password123");

        var response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        UserDTO actual = objectMapper.readValue(body, UserDTO.class);
        User expectedUser = userRepository.findByEmail("newuser@example.com").orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedUser.getId());
        Assertions.assertThat(actual.getEmail()).isEqualTo(expectedUser.getEmail());
        Assertions.assertThat(actual.getFirstName()).isEqualTo(expectedUser.getFirstName());
        Assertions.assertThat(actual.getLastName()).isEqualTo(expectedUser.getLastName());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateOwnProfile() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("UpdatedFirstName"));
        updateDTO.setLastName(JsonNullable.of("UpdatedLastName"));
        updateDTO.setEmail(JsonNullable.of("updated@example.com"));

        var response = mockMvc.perform(put("/api/users/" + testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        UserDTO actual = objectMapper.readValue(body, UserDTO.class);
        User expectedUser = userRepository.findById(testUser.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedUser.getId());
        Assertions.assertThat(actual.getEmail()).isEqualTo(expectedUser.getEmail());
        Assertions.assertThat(actual.getFirstName()).isEqualTo(expectedUser.getFirstName());
        Assertions.assertThat(actual.getLastName()).isEqualTo(expectedUser.getLastName());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteOwnProfile() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }
}
