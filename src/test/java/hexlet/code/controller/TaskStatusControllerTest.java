package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    public void setUp() {
        authToken = jwtUtils.generateToken("hexlet@example.com");
    }

    @Test
    public void testGetTaskStatusesWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTaskStatusByIdWithoutAuth() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Test Status");
        status.setSlug("test_status");
        taskStatusRepository.save(status);

        mockMvc.perform(get("/api/task_statuses/{id}", status.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTaskStatusesWithAuth() throws Exception {
        mockMvc.perform(get("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testGetTaskStatusByIdWithAuth() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Test Status");
        status.setSlug("test_status");
        taskStatusRepository.save(status);

        mockMvc.perform(get("/api/task_statuses/{id}", status.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(status.getId()))
                .andExpect(jsonPath("$.name").value("Test Status"))
                .andExpect(jsonPath("$.slug").value("test_status"));
    }

    @Test
    public void testGetTaskStatusNotFound() throws Exception {
        mockMvc.perform(get("/api/task_statuses/{id}", 999L)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateTaskStatus() throws Exception {
        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("New Status");
        createDTO.setSlug("new_status");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Status"))
                .andExpect(jsonPath("$.slug").value("new_status"));

        assertThat(taskStatusRepository.existsBySlug("new_status")).isTrue();
    }

    @Test
    public void testCreateTaskStatusUnauthenticated() throws Exception {
        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("New Status");
        createDTO.setSlug("new_status");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateTaskStatus() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Old Status");
        status.setSlug("old_status");
        taskStatusRepository.save(status);

        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(org.openapitools.jackson.nullable.JsonNullable.of("Updated Status"));

        mockMvc.perform(put("/api/task_statuses/{id}", status.getId())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Status"))
                .andExpect(jsonPath("$.slug").value("old_status"));
    }

    @Test
    public void testUpdateTaskStatusUnauthenticated() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Old Status");
        status.setSlug("old_status");
        taskStatusRepository.save(status);

        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(org.openapitools.jackson.nullable.JsonNullable.of("Updated Status"));

        mockMvc.perform(put("/api/task_statuses/{id}", status.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteTaskStatus() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("To Delete");
        status.setSlug("to_delete");
        taskStatusRepository.save(status);

        mockMvc.perform(delete("/api/task_statuses/{id}", status.getId())
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(status.getId())).isEmpty();
    }

    @Test
    public void testDeleteTaskStatusUnauthenticated() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("To Delete");
        status.setSlug("to_delete");
        taskStatusRepository.save(status);

        mockMvc.perform(delete("/api/task_statuses/{id}", status.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateTaskStatusWithDuplicateName() throws Exception {
        TaskStatus existingStatus = new TaskStatus();
        existingStatus.setName("Existing Status");
        existingStatus.setSlug("existing_status");
        taskStatusRepository.save(existingStatus);

        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("Existing Status");
        createDTO.setSlug("different_slug");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateTaskStatusWithDuplicateSlug() throws Exception {
        TaskStatus existingStatus = new TaskStatus();
        existingStatus.setName("Existing Name");
        existingStatus.setSlug("existing_slug");
        taskStatusRepository.save(existingStatus);

        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("Different Name");
        createDTO.setSlug("existing_slug");

        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict());
    }
}
