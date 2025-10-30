package hexlet.code.data;

import hexlet.code.model.Role;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Faker faker;

    @PostConstruct
    public void initUsers() {

        if (!userRepository.existsByEmail("hexlet@example.com")) {
            User admin = new User();
            admin.setEmail("hexlet@example.com");
            admin.setPasswordDigest(passwordEncoder.encode("qwerty"));
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
}
