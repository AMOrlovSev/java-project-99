package hexlet.code.repository;

import hexlet.code.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByAssigneeId(Long assigneeId);

    boolean existsByTaskStatusId(Long taskStatusId);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.assignee.id = :userId")
    boolean existsTasksByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) > 0 FROM Task t WHERE t.taskStatus.id = :statusId")
    boolean existsTasksByStatus(@Param("statusId") Long statusId);
}
