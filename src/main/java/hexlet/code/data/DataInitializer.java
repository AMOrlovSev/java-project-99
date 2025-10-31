package hexlet.code.data;

import hexlet.code.configuration.AdminConfig;
import hexlet.code.model.Label;
import hexlet.code.model.Role;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Faker faker;

    @Autowired
    private AdminConfig adminConfig;

    @PostConstruct
    public void initUsers() {
        if (!userRepository.existsByEmail(adminConfig.getEmail())) {
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

        for (int i = 0; i < 5; i++) {
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail(faker.internet().emailAddress());
            var password = faker.internet().password();
            user.setPasswordDigest(passwordEncoder.encode(password));
            user.setRole(Role.USER);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @PostConstruct
    public void initTaskStatuses() {
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
        return status;
    }

    @PostConstruct
    public void initLabels() {
        List<Label> defaultLabels = List.of(
                createLabel("feature"),
                createLabel("bug")
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
        return label;
    }
}
