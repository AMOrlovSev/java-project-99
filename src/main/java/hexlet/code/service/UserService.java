package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAll();
    Optional<User> findById(Long id);
    User create(UserCreateDTO userData);
    User update(Long id, UserUpdateDTO userData);
    void delete(Long id);
}
