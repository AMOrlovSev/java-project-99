package hexlet.code.mapper;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = {JsonNullableMapper.class, ReferenceMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    @Mapping(source = "taskStatus.slug", target = "status")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "labels", target = "taskLabelIds")
    public abstract TaskDTO map(Task model);

    @Mapping(source = "title", target = "name")
    @Mapping(source = "content", target = "description")
    @Mapping(target = "taskStatus", ignore = true)
    @Mapping(target = "assignee", ignore = true)
    @Mapping(target = "labels", ignore = true)
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @AfterMapping
    protected void populateTaskRelationships(@MappingTarget Task task, TaskCreateDTO dto) {
        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            throw new ResourceNotFoundException("Task status is required");
        }

        TaskStatus status = taskStatusRepository.findBySlug(dto.getStatus())
                .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + dto.getStatus()));
        task.setTaskStatus(status);

        if (dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getAssigneeId()));
            task.setAssignee(assignee);
        }

        if (dto.getTaskLabelIds() != null && !dto.getTaskLabelIds().isEmpty()) {
            Set<Label> labels = getLabelsByIds(dto.getTaskLabelIds());
            task.setLabels(labels);
        }
    }

    @AfterMapping
    protected void updateTaskRelationships(TaskUpdateDTO dto, @MappingTarget Task task) {
        if (dto.getStatus() != null && dto.getStatus().isPresent()) {
            String newStatusSlug = dto.getStatus().get();
            TaskStatus status = taskStatusRepository.findBySlug(newStatusSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Task status not found: " + newStatusSlug));
            task.setTaskStatus(status);
        }

        if (dto.getAssigneeId() != null) {
            if (dto.getAssigneeId().isPresent() && dto.getAssigneeId().get() != null) {
                Long newAssigneeId = dto.getAssigneeId().get();
                User assignee = userRepository.findById(newAssigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + newAssigneeId));
                task.setAssignee(assignee);
            } else {
                task.setAssignee(null);
            }
        }

        if (dto.getTaskLabelIds() != null && dto.getTaskLabelIds().isPresent()) {
            Set<Long> newLabelIds = dto.getTaskLabelIds().get();
            Set<Label> newLabels = getLabelsByIds(newLabelIds);

            task.getLabels().clear();
            task.getLabels().addAll(newLabels);
        }
    }

    protected Set<Long> mapLabelsToTaskLabelIds(Set<Label> labels) {
        if (labels == null) {
            return Set.of();
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }

    private Set<Label> getLabelsByIds(Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Label> labels = labelRepository.findAllById(labelIds);

        Set<Long> foundLabelIds = labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());

        Set<Long> missingLabelIds = labelIds.stream()
                .filter(id -> !foundLabelIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingLabelIds.isEmpty()) {
            throw new ResourceNotFoundException("Label not found: " + missingLabelIds);
        }

        return new HashSet<>(labels);
    }
}
