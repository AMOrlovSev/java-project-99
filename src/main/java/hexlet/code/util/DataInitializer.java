package hexlet.code.util;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        if (!userRepository.existsByEmail("hexlet@example.com")) {
            User admin = new User();
            admin.setEmail("hexlet@example.com");
            admin.setPassword(passwordEncoder.encode("qwerty"));
            userRepository.save(admin);
        }

    }
}