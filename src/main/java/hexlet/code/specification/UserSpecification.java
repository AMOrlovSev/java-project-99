package hexlet.code.specification;

import hexlet.code.dto.user.UserParamsDTO;
import hexlet.code.model.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserSpecification {

    public Specification<User> build(UserParamsDTO params) {
        return withId(params.getId())
                .and(withEmail(params.getEmail()))
                .and(withEmailCont(params.getEmailCont()))
                .and(withFirstName(params.getFirstName()))
                .and(withFirstNameCont(params.getFirstNameCont()))
                .and(withLastName(params.getLastName()))
                .and(withLastNameCont(params.getLastNameCont()))
                .and(withCreatedAt(params.getCreatedAt()))
                .and(withCreatedAtGt(params.getCreatedAtGt()))
                .and(withCreatedAtLt(params.getCreatedAtLt()));
    }

    private Specification<User> withId(Long id) {
        return (root, query, cb) -> id == null
                ? cb.conjunction()
                : cb.equal(root.get("id"), id);
    }

    private Specification<User> withEmail(String email) {
        return (root, query, cb) -> email == null
                ? cb.conjunction()
                : cb.equal(root.get("email"), email);
    }

    private Specification<User> withEmailCont(String emailCont) {
        return (root, query, cb) -> emailCont == null
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("email")), "%" + emailCont.toLowerCase() + "%");
    }

    private Specification<User> withFirstName(String firstName) {
        return (root, query, cb) -> firstName == null
                ? cb.conjunction()
                : cb.equal(root.get("firstName"), firstName);
    }

    private Specification<User> withFirstNameCont(String firstNameCont) {
        return (root, query, cb) -> firstNameCont == null
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("firstName")), "%" + firstNameCont.toLowerCase() + "%");
    }

    private Specification<User> withLastName(String lastName) {
        return (root, query, cb) -> lastName == null
                ? cb.conjunction()
                : cb.equal(root.get("lastName"), lastName);
    }

    private Specification<User> withLastNameCont(String lastNameCont) {
        return (root, query, cb) -> lastNameCont == null
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("lastName")), "%" + lastNameCont.toLowerCase() + "%");
    }

    private Specification<User> withCreatedAt(LocalDate createdAt) {
        return (root, query, cb) -> createdAt == null
                ? cb.conjunction()
                : cb.equal(root.get("createdAt"), createdAt);
    }

    private Specification<User> withCreatedAtGt(LocalDate createdAtGt) {
        return (root, query, cb) -> createdAtGt == null
                ? cb.conjunction()
                : cb.greaterThan(root.get("createdAt"), createdAtGt);
    }

    private Specification<User> withCreatedAtLt(LocalDate createdAtLt) {
        return (root, query, cb) -> createdAtLt == null
                ? cb.conjunction()
                : cb.lessThan(root.get("createdAt"), createdAtLt);
    }
}
