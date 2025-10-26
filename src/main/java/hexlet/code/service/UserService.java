package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceAlreadyExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User create(UserCreateDTO userData) {
        if (userRepository.existsByEmail(userData.getEmail())) {
            throw new ResourceAlreadyExistsException("User with email " + userData.getEmail() + " already exists");
        }
        User user = userMapper.map(userData);
        user.setPasswordDigest(passwordEncoder.encode(userData.getPassword()));
        return userRepository.save(user);
    }

    public User update(Long id, UserUpdateDTO userData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (userData.getEmail() != null && userData.getEmail().isPresent()) {
            String newEmail = userData.getEmail().get();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new ResourceAlreadyExistsException("User with email " + newEmail + " already exists");
            }
        }

        userMapper.update(userData, user);

        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            user.setPasswordDigest(passwordEncoder.encode(userData.getPassword().get()));
        }

        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
