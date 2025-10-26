package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserCreateDTO {
    private String firstName;

    private String lastName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 3)
    private String password;
}
