package hexlet.code.controller;

import hexlet.code.DatabaseCleanerExtension;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(DatabaseCleanerExtension.class)
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
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private TaskStatus testStatus;
    private Task testTask;

    @BeforeEach
    void setUp() {
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

        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Test Task");
        createDTO.setContent("Test Description");
        createDTO.setIndex(1);
        createDTO.setStatus("test_status");
        createDTO.setAssigneeId(testUser.getId());

        testTask = taskService.create(createDTO);
    }

    @Test
    @WithMockUser
    void testGetAllTasks() throws Exception {
        var response = mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        List<TaskDTO> actual = objectMapper.readValue(body, new TypeReference<>() { });
        List<Task> expectedTasks = taskRepository.findAll();

        Set<Long> actualIds = actual.stream().map(TaskDTO::getId).collect(Collectors.toSet());
        Set<Long> expectedIds = expectedTasks.stream().map(Task::getId).collect(Collectors.toSet());
        Assertions.assertThat(actualIds).isEqualTo(expectedIds);

        Set<String> actualTitles = actual.stream().map(TaskDTO::getTitle).collect(Collectors.toSet());
        Set<String> expectedNames = expectedTasks.stream().map(Task::getName).collect(Collectors.toSet());
        Assertions.assertThat(actualTitles).isEqualTo(expectedNames);
    }

    @Test
    @WithMockUser
    void testGetTaskById() throws Exception {
        var response = mockMvc.perform(get("/api/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        TaskDTO actual = objectMapper.readValue(body, TaskDTO.class);
        Task expectedTask = taskRepository.findById(testTask.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedTask.getId());
        Assertions.assertThat(actual.getTitle()).isEqualTo(expectedTask.getName());
        Assertions.assertThat(actual.getContent()).isEqualTo(expectedTask.getDescription());
        Assertions.assertThat(actual.getIndex()).isEqualTo(expectedTask.getIndex());
        Assertions.assertThat(actual.getStatus()).isEqualTo(expectedTask.getTaskStatus().getSlug());
        Assertions.assertThat(actual.getAssigneeId()).isEqualTo(expectedTask.getAssignee().getId());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
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

        var response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        TaskDTO actual = objectMapper.readValue(body, TaskDTO.class);
        Task expectedTask = taskRepository.findById(actual.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedTask.getId());
        Assertions.assertThat(actual.getTitle()).isEqualTo(expectedTask.getName());
        Assertions.assertThat(actual.getContent()).isEqualTo(expectedTask.getDescription());
        Assertions.assertThat(actual.getIndex()).isEqualTo(expectedTask.getIndex());
        Assertions.assertThat(actual.getStatus()).isEqualTo(expectedTask.getTaskStatus().getSlug());
        Assertions.assertThat(actual.getAssigneeId()).isEqualTo(expectedTask.getAssignee().getId());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    void testUpdateTask() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(JsonNullable.of("Updated Task"));
        updateDTO.setContent(JsonNullable.of("Updated Description"));
        updateDTO.setIndex(JsonNullable.of(10));

        var response = mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        TaskDTO actual = objectMapper.readValue(body, TaskDTO.class);
        Task expectedTask = taskRepository.findById(testTask.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedTask.getId());
        Assertions.assertThat(actual.getTitle()).isEqualTo(expectedTask.getName());
        Assertions.assertThat(actual.getContent()).isEqualTo(expectedTask.getDescription());
        Assertions.assertThat(actual.getIndex()).isEqualTo(expectedTask.getIndex());
        Assertions.assertThat(actual.getStatus()).isEqualTo(expectedTask.getTaskStatus().getSlug());
        Assertions.assertThat(actual.getAssigneeId()).isEqualTo(expectedTask.getAssignee().getId());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    void testDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", testTask.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(taskRepository.existsById(testTask.getId())).isFalse();
    }

    @Test
    @WithMockUser
    void testCreateTaskWithInvalidStatus() throws Exception {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        createDTO.setTitle("Invalid Task");
        createDTO.setContent("Description");
        createDTO.setStatus("invalid_status");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateTaskWithInvalidStatus() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setStatus(JsonNullable.of("invalid_status"));

        mockMvc.perform(put("/api/tasks/{id}", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testGetNonExistentTask() throws Exception {
        mockMvc.perform(get("/api/tasks/9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateNonExistentTask() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setTitle(JsonNullable.of("Updated"));

        mockMvc.perform(put("/api/tasks/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteNonExistentTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/9999"))
                .andExpect(status().isNotFound());
    }
}
