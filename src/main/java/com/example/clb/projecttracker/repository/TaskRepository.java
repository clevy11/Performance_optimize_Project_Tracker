package com.example.clb.projecttracker.repository;

import com.example.clb.projecttracker.model.Task;
import com.example.clb.projecttracker.model.enums.TaskStatus;
import com.example.clb.projecttracker.dto.TaskStatusCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks by project ID (paginated)
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    // Find tasks by developer ID (paginated)
    Page<Task> findByDeveloperId(Long developerId, Pageable pageable);

    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);

    // Query for overdue tasks (not COMPLETED or CANCELLED and due date is in the past)
    @Query("SELECT t FROM Task t WHERE t.status NOT IN (com.example.clb.projecttracker.model.enums.TaskStatus.COMPLETED, com.example.clb.projecttracker.model.enums.TaskStatus.CANCELLED) AND t.dueDate < CURRENT_DATE")
    Page<Task> findOverdueTasks(Pageable pageable);

    // Find tasks by project ID and status
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    // Find tasks by developer ID and status
    List<Task> findByDeveloperIdAndStatus(Long developerId, TaskStatus status);

    // Count tasks by project ID
    long countByProjectId(Long projectId);

    // Count tasks by developer ID
    long countByDeveloperId(Long developerId);

    // Query to count tasks by status for a specific project
    @Query("SELECT new com.example.clb.projecttracker.dto.TaskStatusCountDto(t.status, COUNT(t)) " +
           "FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<TaskStatusCountDto> countTasksByStatusForProject(@Param("projectId") Long projectId);

    // Query to count tasks by status overall
    @Query("SELECT new com.example.clb.projecttracker.dto.TaskStatusCountDto(t.status, COUNT(t)) " +
           "FROM Task t GROUP BY t.status")
    List<TaskStatusCountDto> countTasksByStatusOverall();

    List<Task> findByProjectId(Long projectId);

    List<Task> findByDeveloperId(Long developerId);

    // Method to find tasks that are overdue and not in a final state (COMPLETED or CANCELLED)
    List<Task> findByDueDateBeforeAndStatusNotIn(LocalDate now, List<TaskStatus> excludedStatuses);

    long countByStatus(TaskStatus status);
}
