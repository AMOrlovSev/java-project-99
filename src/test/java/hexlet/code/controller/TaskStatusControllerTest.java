package hexlet.code.controller;

import hexlet.code.DatabaseCleanerExtension;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.taskStatus.TaskStatusCreateDTO;
import hexlet.code.dto.taskStatus.TaskStatusDTO;
import hexlet.code.dto.taskStatus.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetTaskStatusesWithAuth() throws Exception {
        TaskStatus status1 = new TaskStatus();
        status1.setName("Status 1");
        status1.setSlug("status_1");
        taskStatusRepository.save(status1);

        TaskStatus status2 = new TaskStatus();
        status2.setName("Status 2");
        status2.setSlug("status_2");
        taskStatusRepository.save(status2);

        var response = mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        List<TaskStatusDTO> actual = objectMapper.readValue(body, new TypeReference<>() {

        });
        List<TaskStatus> expectedStatuses = taskStatusRepository.findAll();

        Set<Long> actualIds = actual.stream().map(TaskStatusDTO::getId).collect(Collectors.toSet());
        Set<Long> expectedIds = expectedStatuses.stream().map(TaskStatus::getId).collect(Collectors.toSet());
        Assertions.assertThat(actualIds).isEqualTo(expectedIds);

        Set<String> actualNames = actual.stream().map(TaskStatusDTO::getName).collect(Collectors.toSet());
        Set<String> expectedNames = expectedStatuses.stream().map(TaskStatus::getName).collect(Collectors.toSet());
        Assertions.assertThat(actualNames).isEqualTo(expectedNames);

        actual.forEach(statusDTO -> Assertions.assertThat(statusDTO.getCreatedAt()).isNotNull());
    }

    @Test
    @WithMockUser
    public void testGetTaskStatusByIdWithAuth() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Test Status");
        status.setSlug("test_status");
        taskStatusRepository.save(status);

        var response = mockMvc.perform(get("/api/task_statuses/{id}", status.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        TaskStatusDTO actual = objectMapper.readValue(body, TaskStatusDTO.class);
        TaskStatus expectedStatus = taskStatusRepository.findById(status.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedStatus.getId());
        Assertions.assertThat(actual.getName()).isEqualTo(expectedStatus.getName());
        Assertions.assertThat(actual.getSlug()).isEqualTo(expectedStatus.getSlug());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    public void testCreateTaskStatus() throws Exception {
        TaskStatusCreateDTO createDTO = new TaskStatusCreateDTO();
        createDTO.setName("New Status");
        createDTO.setSlug("new_status");

        var response = mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        TaskStatusDTO actual = objectMapper.readValue(body, TaskStatusDTO.class);
        TaskStatus expectedStatus = taskStatusRepository.findBySlug("new_status").orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedStatus.getId());
        Assertions.assertThat(actual.getName()).isEqualTo(expectedStatus.getName());
        Assertions.assertThat(actual.getSlug()).isEqualTo(expectedStatus.getSlug());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    public void testUpdateTaskStatus() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("Old Status");
        status.setSlug("old_status");
        taskStatusRepository.save(status);

        TaskStatusUpdateDTO updateDTO = new TaskStatusUpdateDTO();
        updateDTO.setName(org.openapitools.jackson.nullable.JsonNullable.of("Updated Status"));

        var response = mockMvc.perform(put("/api/task_statuses/{id}", status.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        var body = response.getContentAsString();

        TaskStatusDTO actual = objectMapper.readValue(body, TaskStatusDTO.class);
        TaskStatus expectedStatus = taskStatusRepository.findById(status.getId()).orElseThrow();

        Assertions.assertThat(actual.getId()).isEqualTo(expectedStatus.getId());
        Assertions.assertThat(actual.getName()).isEqualTo(expectedStatus.getName());
        Assertions.assertThat(actual.getSlug()).isEqualTo(expectedStatus.getSlug());
        Assertions.assertThat(actual.getCreatedAt()).isNotNull();
    }

    @Test
    @WithMockUser
    public void testDeleteTaskStatus() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("To Delete");
        status.setSlug("to_delete");
        taskStatusRepository.save(status);

        mockMvc.perform(delete("/api/task_statuses/{id}", status.getId()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(taskStatusRepository.existsById(status.getId())).isFalse();
    }
}
