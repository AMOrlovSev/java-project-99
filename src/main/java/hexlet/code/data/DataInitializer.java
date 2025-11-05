package hexlet.code.data;

import hexlet.code.configuration.AdminConfig;
import hexlet.code.model.Label;
import hexlet.code.model.Role;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminConfig adminConfig;

    @PostConstruct
    @Transactional
    public void init() {
        // Проверяем, что все зависимости инициализированы
        if (userRepository == null || taskStatusRepository == null || labelRepository == null
                || taskRepository == null || passwordEncoder == null || adminConfig == null) {
            return;
        }

        initUsers();
        initTaskStatuses();
        initLabels();
        initTasks();
    }

    private void initUsers() {
        // Администратор
        if (adminConfig.getEmail() != null && !userRepository.existsByEmail(adminConfig.getEmail())) {
            User admin = new User();
            admin.setEmail(adminConfig.getEmail());
            admin.setPasswordDigest(passwordEncoder.encode(adminConfig.getPassword()));
            admin.setFirstName("Hexlet");
            admin.setLastName("Admin");
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
        }

        // 3 конкретных пользователя
        createUserIfNotExists("alice.smith@example.com", "Alice", "Smith", "password123", Role.USER);
        createUserIfNotExists("bob.johnson@example.com", "Bob", "Johnson", "password123", Role.USER);
        createUserIfNotExists("carol.williams@example.com", "Carol", "Williams", "password123", Role.USER);
    }

    private void createUserIfNotExists(String email, String firstName, String lastName, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPasswordDigest(passwordEncoder.encode(password));
            user.setRole(role);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    private void initTaskStatuses() {
        List<TaskStatus> defaultStatuses = List.of(
                createTaskStatus("Draft", "draft"),
                createTaskStatus("ToReview", "to_review"),
                createTaskStatus("ToBeFixed", "to_be_fixed"),
                createTaskStatus("ToPublish", "to_publish"),
                createTaskStatus("Published", "published")
        );

        defaultStatuses.forEach(status -> {
            if (!taskStatusRepository.existsBySlug(status.getSlug())) {
                taskStatusRepository.save(status);
            }
        });
    }

    private TaskStatus createTaskStatus(String name, String slug) {
        TaskStatus status = new TaskStatus();
        status.setName(name);
        status.setSlug(slug);
        status.setCreatedAt(LocalDateTime.now());
        return status;
    }

    private void initLabels() {
        List<Label> defaultLabels = List.of(
                createLabel("feature"),
                createLabel("bug"),
                createLabel("urgent"),
                createLabel("documentation")
        );

        defaultLabels.forEach(label -> {
            if (!labelRepository.existsByName(label.getName())) {
                labelRepository.save(label);
            }
        });
    }

    private Label createLabel(String name) {
        Label label = new Label();
        label.setName(name);
        label.setCreatedAt(LocalDateTime.now());
        return label;
    }

    private void initTasks() {
        // Проверяем, есть ли уже задачи
        if (taskRepository.count() > 0) {
            return;
        }

        // Получаем пользователей
        User alice = userRepository.findByEmail("alice.smith@example.com").orElse(null);
        User bob = userRepository.findByEmail("bob.johnson@example.com").orElse(null);
        User carol = userRepository.findByEmail("carol.williams@example.com").orElse(null);
        User admin = userRepository.findByEmail(adminConfig.getEmail()).orElse(null);

        // Получаем статусы
        TaskStatus draft = taskStatusRepository.findBySlug("draft").orElse(null);
        TaskStatus toReview = taskStatusRepository.findBySlug("to_review").orElse(null);
        TaskStatus toBeFixed = taskStatusRepository.findBySlug("to_be_fixed").orElse(null);
        TaskStatus toPublish = taskStatusRepository.findBySlug("to_publish").orElse(null);
        TaskStatus published = taskStatusRepository.findBySlug("published").orElse(null);

        // Получаем метки
        Label feature = labelRepository.findByName("feature").orElse(null);
        Label bug = labelRepository.findByName("bug").orElse(null);
        Label urgent = labelRepository.findByName("urgent").orElse(null);
        Label documentation = labelRepository.findByName("documentation").orElse(null);

        // Проверяем, что все необходимые данные доступны
        if (alice == null || bob == null || carol == null || admin == null
                || draft == null || toReview == null || toBeFixed == null
                || toPublish == null || published == null) {
            System.out.println("Skipping task initialization - required data not available");
            return;
        }

        // Создаем 8 задач
        createTask("Implement user authentication",
                "Develop secure login and registration system",
                1, draft, alice, bob, feature != null ? Set.of(feature) : Set.of());

        createTask("Fix payment processing bug",
                "Payment gateway integration is failing for certain cards",
                2, toBeFixed, bob, carol, bug != null && urgent != null ? Set.of(bug, urgent) : Set.of());

        createTask("Design new dashboard UI",
                "Create modern and responsive dashboard interface",
                3, toReview, carol, alice, feature != null ? Set.of(feature) : Set.of());

        createTask("Write API documentation",
                "Document all REST API endpoints with examples",
                4, toPublish, admin, alice, documentation != null ? Set.of(documentation) : Set.of());

        createTask("Research new technologies",
                "Investigate potential new frameworks and libraries",
                5, draft, alice, null, Set.of());

        createTask("Performance optimization",
                "Improve application performance and reduce load times",
                6, published, bob, carol, feature != null && urgent != null ? Set.of(feature, urgent) : Set.of());

        createTask("Database migration",
                "Migrate from MySQL to PostgreSQL",
                7, toBeFixed, carol, bob, feature != null ? Set.of(feature) : Set.of());

        createTask("Security audit",
                "Conduct comprehensive security review of the application",
                8, toReview, admin, alice, urgent != null ? Set.of(urgent) : Set.of());
    }

    private void createTask(String name, String description, Integer index,
                            TaskStatus status, User creator, User assignee, Set<Label> labels) {
        Task task = new Task();
        task.setName(name);
        task.setDescription(description);
        task.setIndex(index);
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        task.setCreatedAt(LocalDateTime.now());

        if (labels != null && !labels.isEmpty()) {
            task.setLabels(labels);
        }

        taskRepository.save(task);
    }
}
