package hexlet.code.specification;

import hexlet.code.DatabaseCleanerExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(DatabaseCleanerExtension.class)
public class TaskFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTUtils jwtUtils;

    private User testUser1;
    private User testUser2;
    private TaskStatus statusToDo;
    private TaskStatus statusInProgress;
    private TaskStatus statusDone;
    private Label bugLabel;
    private Label featureLabel;
    private Label enhancementLabel;
    private Task task1;
    private Task task2;
    private Task task3;
    private String authToken;

    @BeforeEach
    void setUp() {

        testUser1 = createUser("filtertest1@example.com", "Filter", "UserOne");
        testUser2 = createUser("filtertest2@example.com", "Filter", "UserTwo");

        statusToDo = createTaskStatus("To Do Filter", "to_do_filter");
        statusInProgress = createTaskStatus("In Progress Filter", "in_progress_filter");
        statusDone = createTaskStatus("Done Filter", "done_filter");

        bugLabel = labelRepository.findByName("bug")
                .orElseGet(() -> createLabel("bug_filter_test"));
        featureLabel = labelRepository.findByName("feature")
                .orElseGet(() -> createLabel("feature_filter_test"));
        enhancementLabel = createLabel("enhancement_filter_test");

        task1 = createTask("Create user authentication filter",
                "Implement JWT authentication filtering",
                1, statusToDo, testUser1, Set.of(bugLabel));
        task2 = createTask("Add task filtering feature",
                "Implement task filtering functionality",
                2, statusInProgress, testUser2, Set.of(featureLabel));
        task3 = createTask("Fix user creation filter enhancement",
                "Fix bug in user creation filter endpoint",
                3, statusDone, testUser1, Set.of(bugLabel, featureLabel, enhancementLabel));

        authToken = jwtUtils.generateToken(testUser1.getEmail());
    }

    private User createUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordDigest("password");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private TaskStatus createTaskStatus(String name, String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseGet(() -> {
                    TaskStatus status = new TaskStatus();
                    status.setName(name);
                    status.setSlug(slug);
                    status.setCreatedAt(LocalDateTime.now());
                    return taskStatusRepository.save(status);
                });
    }

    private Label createLabel(String name) {
        return labelRepository.findByName(name)
                .orElseGet(() -> {
                    Label label = new Label();
                    label.setName(name);
                    return labelRepository.save(label);
                });
    }

    private Task createTask(String name, String description,
                            int index, TaskStatus status, User assignee, Set<Label> labels) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setIndex(index);
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        task.setLabels(labels);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    @Test
    @WithMockUser
    void testGetAllTasksWithoutFilters() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser
    void testFilterByTitleContaining() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].title").value("Create user authentication filter"))
                .andExpect(jsonPath("$[1].title").value("Add task filtering feature"))
                .andExpect(jsonPath("$[2].title").value("Fix user creation filter enhancement"));
    }

    @Test
    @WithMockUser
    void testFilterByTitleContainingSpecific() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "authentication")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("Create user authentication filter"));
    }

    @Test
    @WithMockUser
    void testFilterByTitleContainingCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "AUTHENTICATION")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("Create user authentication filter"));
    }

    @Test
    @WithMockUser
    void testFilterByAssigneeId() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("assigneeId", testUser1.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(jsonPath("$[0].assigneeId").value(testUser1.getId()))
                .andExpect(jsonPath("$[1].assigneeId").value(testUser1.getId()));
    }

    @Test
    @WithMockUser
    void testFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("status", "in_progress_filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].status").value("in_progress_filter"))
                .andExpect(jsonPath("$[0].title").value("Add task filtering feature"));
    }

    @Test
    @WithMockUser
    void testFilterByLabelId() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("labelId", enhancementLabel.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("Fix user creation filter enhancement"));
    }

    @Test
    @WithMockUser
    void testFilterByMultipleParameters() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "enhancement")
                        .param("assigneeId", testUser1.getId().toString())
                        .param("status", "done_filter")
                        .param("labelId", enhancementLabel.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("Fix user creation filter enhancement"))
                .andExpect(jsonPath("$[0].assigneeId").value(testUser1.getId()))
                .andExpect(jsonPath("$[0].status").value("done_filter"));
    }

    @Test
    @WithMockUser
    void testFilterByTitleAndStatus() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "filtering")
                        .param("status", "in_progress_filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("Add task filtering feature"))
                .andExpect(jsonPath("$[0].status").value("in_progress_filter"));
    }

    @Test
    @WithMockUser
    void testFilterByAssigneeAndLabel() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("assigneeId", testUser1.getId().toString())
                        .param("labelId", featureLabel.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("Fix user creation filter enhancement"))
                .andExpect(jsonPath("$[0].assigneeId").value(testUser1.getId()));
    }

    @Test
    @WithMockUser
    void testFilterWithNoResults() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "nonexistenttask")
                        .param("status", "to_do_filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testFilterWithEmptyParameters() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "")
                        .param("assigneeId", "")
                        .param("status", "")
                        .param("labelId", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @WithMockUser
    void testFilterWithPartialMatch() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "creat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(jsonPath("$[0].title").value("Create user authentication filter"))
                .andExpect(jsonPath("$[1].title").value("Fix user creation filter enhancement"));
    }

    @Test
    @WithMockUser
    void testFilterTasksWithoutAssignee() throws Exception {
        Task unassignedTask = createTask("Unassigned filter task",
                "No assignee for filter test", 4, statusToDo, null, Set.of());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("assigneeId", "0") // Несуществующий ID
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testPaginationWithFilters() throws Exception {
        for (int i = 4; i <= 15; i++) {
            createTask("Filter Task " + i, "Description " + i, i, statusToDo, testUser1, Set.of(bugLabel));
        }

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "Filter Task")
                        .param("page", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "12"))
                .andExpect(jsonPath("$.length()").value(10)); // 10 задач на странице

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "Filter Task")
                        .param("page", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "12"))
                .andExpect(jsonPath("$.length()").value(2)); // Оставшиеся 2 задачи
    }

    @Test
    @WithMockUser
    void testFilterWithInvalidAssigneeId() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("assigneeId", "9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testFilterWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("status", "invalid_status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    void testFilterWithInvalidLabelId() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("labelId", "9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "0"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testFilterTasksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("titleCont", "filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testFilterExcludingSomeTasks() throws Exception {
        Task nonFilterTask = createTask("Regular task",
                "This is a regular task without filter word", 99, statusToDo, testUser1, Set.of());

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + authToken)
                        .param("titleCont", "filter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "3"))
                .andExpect(jsonPath("$.length()").value(3));
    }
}
