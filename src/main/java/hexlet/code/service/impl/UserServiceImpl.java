package hexlet.code.service.impl;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceAlreadyExistsException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TaskService;
import hexlet.code.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TaskService taskService;

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User create(UserCreateDTO userData) {
        if (userRepository.existsByEmail(userData.getEmail())) {
            throw new ResourceAlreadyExistsException("User with email " + userData.getEmail() + " already exists");
        }
        User user = userMapper.map(userData);
        user.setPasswordDigest(passwordEncoder.encode(userData.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, UserUpdateDTO userData) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (userData.getEmail() != null && userData.getEmail().isPresent()) {
            String newEmail = userData.getEmail().get();
            if (!newEmail.equals(userToUpdate.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new ResourceAlreadyExistsException("User with email " + newEmail + " already exists");
            }
        }

        userMapper.update(userData, userToUpdate);

        if (userData.getPassword() != null && userData.getPassword().isPresent()) {
            userToUpdate.setPasswordDigest(passwordEncoder.encode(userData.getPassword().get()));
        }

        return userRepository.save(userToUpdate);
    }

    @Override
    public void delete(Long id) {
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (taskService.hasTasksWithUser(id)) {
            throw new ResourceAlreadyExistsException("Cannot delete user with associated tasks");
        }

        userRepository.deleteById(id);
    }
}
