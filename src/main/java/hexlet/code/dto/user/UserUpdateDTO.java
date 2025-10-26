package hexlet.code.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class UserUpdateDTO {
    private JsonNullable<String> firstName;

    private JsonNullable<String> lastName;

    @Email
    private JsonNullable<String> email;

    @Size(min = 3)
    private JsonNullable<String> password;
}
