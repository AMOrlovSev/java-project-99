package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
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

    private User testUser;

    @BeforeEach
    public void setUp() {
        // Очищаем базу перед каждым тестом
        userRepository.deleteAll();

        testUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getPassword), () -> faker.internet().password(8, 16))
                .create();
    }

    @Test
    public void testIndex() throws Exception {
        userRepository.save(testUser);

        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var userCreateDTO = new UserCreateDTO();
        userCreateDTO.setFirstName(testUser.getFirstName());
        userCreateDTO.setLastName(testUser.getLastName());
        userCreateDTO.setEmail(testUser.getEmail());
        userCreateDTO.setPassword(testUser.getPassword());

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userCreateDTO));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(testUser.getEmail()).orElse(null);
        assertNotNull(user);
        assertThat(user.getFirstName()).isEqualTo(userCreateDTO.getFirstName());
        assertThat(user.getEmail()).isEqualTo(userCreateDTO.getEmail());
    }

    @Test
    public void testShow() throws Exception {
        userRepository.save(testUser);

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
    public void testUpdate() throws Exception {
        userRepository.save(testUser);

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
    public void testPartialUpdate() throws Exception {
        userRepository.save(testUser);

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
    public void testDestroy() throws Exception {
        userRepository.save(testUser);

        var request = delete("/api/users/" + testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(testUser.getId())).isFalse();
    }

    @Test
    public void testCreateWithDuplicateEmail() throws Exception {
        userRepository.save(testUser);

        var duplicateUserDTO = new UserCreateDTO();
        duplicateUserDTO.setFirstName("Another");
        duplicateUserDTO.setLastName("User");
        duplicateUserDTO.setEmail(testUser.getEmail());
        duplicateUserDTO.setPassword("password123");

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(duplicateUserDTO));

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testUpdateWithDuplicateEmail() throws Exception {
        var user1 = userRepository.save(testUser);

        var user2 = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .create();
        userRepository.save(user2);

        var updateDTO = new UserUpdateDTO();
        updateDTO.setEmail(JsonNullable.of(user1.getEmail()));

        var request = put("/api/users/" + user2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }

    @Test
    public void testShowNonExistentUser() throws Exception {
        var request = get("/api/users/999999");
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateNonExistentUser() throws Exception {
        var updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName(JsonNullable.of("NonExistent"));

        var request = put("/api/users/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }
}