package hexlet.code.controller;

import hexlet.code.DatabaseCleanerExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.authentication.AuthRequest;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.Role;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private ObjectMapper om;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        user.setPasswordDigest(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createTestUser(String email) {
        return createUser(email, "Test", "User", "password123", Role.USER);
    }

    @Test
    public void testAccessUsersListWithoutToken() throws Exception {
        var request = get("/api/users");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccessUserByIdWithoutToken() throws Exception {
        var request = get("/api/users/" + testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateUserWithoutToken() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("Unauthorized"));

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteUserWithoutToken() throws Exception {
        var request = delete("/api/users/" + testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateUserWithoutToken() throws Exception {
        var userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("New");
        userCreateDTO.setLastName("User");
        userCreateDTO.setEmail("newuser@example.com");
        userCreateDTO.setPassword("password123");

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail("newuser@example.com").orElse(null);
        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(userCreateDTO.getFirstName());
        assertThat(user.getEmail()).isEqualTo(userCreateDTO.getEmail());
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    public void testLoginWithoutToken() throws Exception {
        var authRequest = new AuthRequest();
        authRequest.setUsername("test@example.com");
        authRequest.setPassword("password123");

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(authRequest));

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testIndex() throws Exception {
        var request = get("/api/users");

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testShow() throws Exception {
        var request = get("/api/users/" + testUser.getId());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testUser.getId()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("email").isEqualTo(testUser.getEmail())
        );
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testUpdateOwnProfile() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("UpdatedFirstName"));
        updateDTO.setLastName(JsonNullable.of("UpdatedLastName"));
        updateDTO.setEmail(JsonNullable.of("updated@example.com"));

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(updatedUser.getLastName()).isEqualTo("UpdatedLastName");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    public void testUpdateOtherUserAsUser() throws Exception {
        User otherUser = createTestUser("other@example.com");

        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("Hacked"));

        var request = put("/api/users/" + otherUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    public void testUpdateUserAsAdmin() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("AdminUpdated"));
        updateDTO.setLastName(JsonNullable.of("ByAdmin"));

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertThat(updatedUser.getFirstName()).isEqualTo("AdminUpdated");
        assertThat(updatedUser.getLastName()).isEqualTo("ByAdmin");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testPartialUpdate() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("PartiallyUpdated"));

        var request = put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertThat(updatedUser.getFirstName()).isEqualTo("PartiallyUpdated");
        assertThat(updatedUser.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testDeleteOwnProfile() throws Exception {
        var request = delete("/api/users/" + testUser.getId());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    public void testDeleteOtherUserAsUser() throws Exception {
        User otherUser = createTestUser("other@example.com");

        var request = delete("/api/users/" + otherUser.getId());

        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    public void testDeleteUserAsAdmin() throws Exception {
        User userToDelete = createTestUser("todelete@example.com");

        var request = delete("/api/users/" + userToDelete.getId());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(userToDelete.getId())).isFalse();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    public void testShowOtherUser() throws Exception {
        User otherUser = createTestUser("other@example.com");

        final Long otherUserId = otherUser.getId();
        final String otherUserFirstName = otherUser.getFirstName();
        final String otherUserLastName = otherUser.getLastName();
        final String otherUserEmail = otherUser.getEmail();

        var request = get("/api/users/" + otherUserId);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(otherUserId),
                v -> v.node("firstName").isEqualTo(otherUserFirstName),
                v -> v.node("lastName").isEqualTo(otherUserLastName),
                v -> v.node("email").isEqualTo(otherUserEmail)
        );
    }
}
