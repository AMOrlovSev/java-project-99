package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtils jwtUtils;

    private User testUser;
    private TaskStatus testStatus;
    private Task testTask;
    private String authToken;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordDigest("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        testStatus = new TaskStatus();
        testStatus.setName("Test Status");
        testStatus.setSlug("test_status");
        testStatus.setCreatedAt(LocalDateTime.now());
        testStatus = taskStatusRepository.save(testStatus);

        testTask = new Task();
        testTask.setName("Test Task");
        testTask.setDescription("Test Description");
        testTask.setIndex(1);
        testTask.setTaskStatus(testStatus);
        testTask.setAssignee(testUser);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask = taskRepository.save(testTask);

        authToken = jwtUtils.generateToken(testUser.getEmail());
    }

    @Test
    @WithMockUser
    void testGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].id").value(testTask.getId()))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].content").value("Test Description"))
                .andExpect(jsonPath("$[0].index").value(1))
                .andExpect(jsonPath("$[0].status").value("test_status"))
                .andExpect(jsonPath("$[0].assigneeId").value(testUser.getId()));
    }

    @Test
    @WithMockUser
    void testGetTaskById() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.content").value("Test Description"))
                .andExpect(jsonPath("$.index").value(1))
                .andExpect(jsonPath("$.status").value("test_status"))
                .andExpect(jsonPath("$.assigneeId").value(testUser.getId()));
    }

    @Test
    @WithMockUser
    void testGetTaskByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testCreateTask() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("New Task");
        createDTO.setContent("New Description");
        createDTO.setIndex(2);
        createDTO.setStatus("test_status");
        createDTO.setAssigneeId(testUser.getId());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.content").value("New Description"))
                .andExpect(jsonPath("$.index").value(2))
                .andExpect(jsonPath("$.status").value("test_status"))
                .andExpect(jsonPath("$.assigneeId").value(testUser.getId()));

        assertThat(taskRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser
    void testCreateTaskWithoutAssignee() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Task Without Assignee");
        createDTO.setContent("Description");
        createDTO.setIndex(3);
        createDTO.setStatus("test_status");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Task Without Assignee"))
                .andExpect(jsonPath("$.content").value("Description"))
                .andExpect(jsonPath("$.index").value(3))
                .andExpect(jsonPath("$.status").value("test_status"))
                .andExpect(jsonPath("$.assigneeId").doesNotExist());
    }

    @Test
    @WithMockUser
    void testCreateTaskWithInvalidStatus() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("New Task");
        createDTO.setContent("New Description");
        createDTO.setStatus("invalid_status"); // Несуществующий статус

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testCreateTaskWithInvalidAssignee() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("New Task");
        createDTO.setContent("New Description");
        createDTO.setStatus("test_status");
        createDTO.setAssigneeId(999L);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testCreateTaskValidation() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();

        createDTO.setContent("Description");
        createDTO.setStatus("test_status");

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testUpdateTask() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(JsonNullable.of("Updated Task"));
        updateDTO.setContent(JsonNullable.of("Updated Description"));
        updateDTO.setIndex(JsonNullable.of(10));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.content").value("Updated Description"))
                .andExpect(jsonPath("$.index").value(10))
                .andExpect(jsonPath("$.status").value("test_status"))
                .andExpect(jsonPath("$.assigneeId").value(testUser.getId()));
    }

    @Test
    @WithMockUser
    void testUpdateTaskStatus() throws Exception {
        TaskStatus newStatus = new TaskStatus();
        newStatus.setName("New Status");
        newStatus.setSlug("new_status");
        newStatus.setCreatedAt(LocalDateTime.now());
        newStatus = taskStatusRepository.save(newStatus);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setStatus(JsonNullable.of("new_status"));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("new_status"));
    }

    @Test
    @WithMockUser
    void testUpdateTaskAssignee() throws Exception {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordDigest("password");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser = userRepository.save(newUser);

        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssigneeId(JsonNullable.of(newUser.getId()));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(newUser.getId()));
    }

    @Test
    @WithMockUser
    void testRemoveTaskAssignee() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setAssigneeId(JsonNullable.of(null));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").doesNotExist());

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getAssignee()).isNull();
    }

    @Test
    @WithMockUser
    void testUpdateTaskNotFound() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(JsonNullable.of("Updated Task"));

        mockMvc.perform(put("/api/tasks/{id}", 999L)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.count()).isEqualTo(0);
        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    @WithMockUser
    void testDeleteTaskNotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTasksWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCreateTaskWithoutAuth() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("New Task");
        createDTO.setContent("New Description");
        createDTO.setStatus("test_status");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateTaskWithoutAuth() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(JsonNullable.of("Updated Task"));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteTaskWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isUnauthorized());
    }
}
