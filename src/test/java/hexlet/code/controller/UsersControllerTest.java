package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.authentication.AuthRequest;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.JWTUtils;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired
    private JWTUtils jwtUtils;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    public void setUp() {
        // Очищаем базу перед каждым тестом
        userRepository.deleteAll();

        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getPasswordDigest), () -> passwordEncoder.encode("password123"))
                .create();

        // Сохраняем пользователя и генерируем токен для него
        userRepository.save(testUser);
        jwtToken = jwtUtils.generateToken(testUser.getEmail());
    }

    private String getAuthHeader() {
        return "Bearer " + jwtToken;
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/users")
                .header("Authorization", getAuthHeader());

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName("New");
        userCreateDTO.setLastName("User");
        userCreateDTO.setEmail("newuser@example.com");
        userCreateDTO.setPassword("password123");

        var request = post("/api/users")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail("newuser@example.com").orElse(null);
        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(userCreateDTO.getFirstName());
        assertThat(user.getEmail()).isEqualTo(userCreateDTO.getEmail());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/users/" + testUser.getId())
                .header("Authorization", getAuthHeader());

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
    public void testUpdate() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("UpdatedFirstName"));
        updateDTO.setLastName(JsonNullable.of("UpdatedLastName"));
        updateDTO.setEmail(JsonNullable.of("updated@example.com"));

        var request = put("/api/users/" + testUser.getId())
                .header("Authorization", getAuthHeader())
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
    public void testPartialUpdate() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("PartiallyUpdated"));

        var request = put("/api/users/" + testUser.getId())
                .header("Authorization", getAuthHeader())
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
    public void testDestroy() throws Exception {
        var request = delete("/api/users/" + testUser.getId())
                .header("Authorization", getAuthHeader());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    public void testCreateWithDuplicateEmail() throws Exception {
        var duplicateUserDTO = new UserCreateDTO();
        duplicateUserDTO.setFirstName("Another");
        duplicateUserDTO.setLastName("User");
        duplicateUserDTO.setEmail(testUser.getEmail()); // Дублирующий email
        duplicateUserDTO.setPassword("password123");

        var request = post("/api/users")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(duplicateUserDTO));

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateWithDuplicateEmail() throws Exception {
        // Создаем второго пользователя
        var user2 = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> passwordEncoder.encode("password123"))
                .create();
        userRepository.save(user2);

        // Пытаемся обновить email второго пользователя на email первого
        var updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of(testUser.getEmail()));

        var request = put("/api/users/" + user2.getId())
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testShowNonExistentUser() throws Exception {
        var request = get("/api/users/999999")
                .header("Authorization", getAuthHeader());

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateNonExistentUser() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("NonExistent"));

        var request = put("/api/users/999999")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAccessWithoutToken() throws Exception {
        // Тест на доступ без токена
        var request = get("/api/users");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAccessWithInvalidToken() throws Exception {
        // Тест на доступ с невалидным токеном
        var request = get("/api/users")
                .header("Authorization", "Bearer invalid-token");

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin() throws Exception {
        var authRequest = new AuthRequest();
        authRequest.setUsername(testUser.getEmail());
        authRequest.setPassword("password123"); // Пароль, который мы закодировали в setUp

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(authRequest));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var token = result.getResponse().getContentAsString();
        assertThat(token).isNotEmpty();
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        var authRequest = new AuthRequest();
        authRequest.setUsername(testUser.getEmail());
        authRequest.setPassword("wrong-password");

        var request = post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(authRequest));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
