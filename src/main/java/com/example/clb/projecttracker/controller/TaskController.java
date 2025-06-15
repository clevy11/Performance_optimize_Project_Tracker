package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.TaskDto;
import com.example.clb.projecttracker.dto.TaskRequestDto;
import com.example.clb.projecttracker.dto.TaskStatusCountDto;
import com.example.clb.projecttracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create a new task", 
               description = "Creates a new task. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskDto createdTask = taskService.createTask(taskRequestDto);
        return ResponseEntity.created(URI.create("/api/tasks/" + createdTask.getId()))
                .body(createdTask);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @taskSecurityService.canViewTask(#taskId)")
    @Operation(summary = "Get task by ID", 
               description = "Retrieves a task by its ID. Developers can only view tasks assigned to them.")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long taskId) {
        TaskDto taskDto = taskService.getTaskById(taskId);
        return ResponseEntity.ok(taskDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get all tasks", 
               description = "Retrieves all tasks with pagination. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get tasks by project ID", 
               description = "Retrieves tasks for a specific project. CONTRACTOR role has read-only access.")
    public ResponseEntity<Page<TaskDto>> getTasksByProjectId(
            @PathVariable Long projectId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByProjectId(projectId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Get current user's tasks", 
               description = "Retrieves tasks assigned to the current logged-in developer.")
    public ResponseEntity<Page<TaskDto>> getMyTasks(
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksForCurrentUser(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/developer/{developerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or (hasRole('DEVELOPER') and #developerId == authentication.principal.id)")
    @Operation(summary = "Get tasks by developer ID", 
               description = "Retrieves tasks for a specific developer. Developers can only view their own tasks.")
    public ResponseEntity<Page<TaskDto>> getTasksByDeveloperId(
            @PathVariable Long developerId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByDeveloperId(developerId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get overdue tasks", 
               description = "Retrieves overdue tasks with pagination. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<Page<TaskDto>> getOverdueTasks(
            @PageableDefault(size = 20, sort = "dueDate,asc") Pageable pageable) {
        Page<TaskDto> overdueTasks = taskService.getOverdueTasks(pageable);
        return ResponseEntity.ok(overdueTasks);
    }

    @GetMapping("/overdue/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get all overdue tasks (not paginated)",
               description = "Retrieves a list of all tasks that are past their due date and are not yet completed or cancelled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of overdue tasks")
    })
    public ResponseEntity<List<TaskDto>> getOverdueTasksList() {
        return ResponseEntity.ok(taskService.findOverdueTasks());
    }

    @GetMapping("/projects/{projectId}/status-counts")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get task counts by status for a specific project")
    public ResponseEntity<List<TaskStatusCountDto>> getTaskCountsByStatusForProject(@PathVariable Long projectId) {
        List<TaskStatusCountDto> counts = taskService.getTaskCountsByStatusForProject(projectId);
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/status-counts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get overall task status counts", 
               description = "Retrieves task counts by status across all projects. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<List<TaskStatusCountDto>> getTaskCountsByStatusOverall() {
        List<TaskStatusCountDto> counts = taskService.getTaskCountsByStatusOverall();
        return ResponseEntity.ok(counts);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @taskSecurityService.canUpdateTask(#taskId)")
    @Operation(summary = "Update a task", 
               description = "Updates a task. Developers can only update tasks assigned to them.")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskDto updatedTask = taskService.updateTask(taskId, taskRequestDto);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/assign/{developerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Assign task to developer", 
               description = "Assigns a task to a developer. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<TaskDto> assignTaskToDeveloper(@PathVariable Long taskId, @PathVariable Long developerId) {
        TaskDto updatedTask = taskService.assignTaskToDeveloper(taskId, developerId);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/unassign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Unassign task from developer", 
               description = "Removes task assignment from developer. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<TaskDto> unassignTaskFromDeveloper(@PathVariable Long taskId) {
        TaskDto updatedTask = taskService.unassignTaskFromDeveloper(taskId);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a task", 
               description = "Deletes a task. Only accessible by ADMIN role.")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
