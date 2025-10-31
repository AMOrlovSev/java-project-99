package hexlet.code.specification;

import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.JoinType;

@Component
public class TaskSpecification {

    public Specification<Task> build(TaskParamsDTO params) {
        return withTitleCont(params.getTitleCont())
                .and(withAssigneeId(params.getAssigneeId()))
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    private Specification<Task> withTitleCont(String titleCont) {
        return (root, query, cb) -> {
            if (titleCont == null || titleCont.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("name")), "%" + titleCont.toLowerCase() + "%");
        };
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("taskStatus").get("slug"), status);
        };
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) -> {
            if (labelId == null) {
                return cb.conjunction();
            }
            var labelJoin = root.join("labels", JoinType.INNER);
            return cb.equal(labelJoin.get("id"), labelId);
        };
    }
}
