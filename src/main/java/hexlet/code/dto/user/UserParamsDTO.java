package hexlet.code.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class UserParamsDTO {
    private Long id;
    private String email;
    private String emailCont;
    private String firstName;
    private String firstNameCont;
    private String lastName;
    private String lastNameCont;
    private LocalDate createdAt;
    private LocalDate createdAtGt;
    private LocalDate createdAtLt;
}
