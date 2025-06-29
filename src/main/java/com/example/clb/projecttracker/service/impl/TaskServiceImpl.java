package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.document.enums.ActionType;
import com.example.clb.projecttracker.dto.TaskDto;
import com.example.clb.projecttracker.dto.TaskRequestDto;
import com.example.clb.projecttracker.dto.TaskStatusCountDto;
import com.example.clb.projecttracker.dto.TaskSummaryDto;
import com.example.clb.projecttracker.exception.ResourceNotFoundException;
import com.example.clb.projecttracker.mapper.TaskMapper;
import com.example.clb.projecttracker.model.Developer;
import com.example.clb.projecttracker.model.Project;
import com.example.clb.projecttracker.model.Task;
import com.example.clb.projecttracker.model.enums.TaskStatus;
import com.example.clb.projecttracker.repository.DeveloperRepository;
import com.example.clb.projecttracker.repository.ProjectRepository;
import com.example.clb.projecttracker.repository.TaskRepository;
import com.example.clb.projecttracker.service.AuditLogService;
import com.example.clb.projecttracker.service.TaskService;
import com.example.clb.projecttracker.security.SecurityUtil;
import com.example.clb.projecttracker.security.UserPrincipal;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final AuditLogService auditLogService;
    private final TaskMapper taskMapper;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "tasksPage", allEntries = true),
        @CacheEvict(value = "tasksByProjectPages", allEntries = true),
        @CacheEvict(value = "tasksByDeveloperPages", allEntries = true)
    })
    public TaskDto createTask(TaskRequestDto taskRequestDto) {
        Project project = projectRepository.findById(taskRequestDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", taskRequestDto.getProjectId()));

        Developer developer = null;
        if (taskRequestDto.getDeveloperId() != null) {
            developer = developerRepository.findById(taskRequestDto.getDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer", "id", taskRequestDto.getDeveloperId()));
        }

        Task task = mapToEntity(taskRequestDto, project, developer);
        Task savedTask = taskRepository.save(task);
        String developerName = developer != null ? developer.getName() : "Unassigned";
        auditLogService.logAction("Task", savedTask.getId(), ActionType.CREATED, "SYSTEM",
                String.format("Task created: '%s' for Project '%s', Assigned to: '%s'", savedTask.getTitle(), project.getName(), developerName));
        return mapToDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasks", key = "#taskId")
    public TaskDto getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return mapToDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("tasksPage")
    public Page<TaskDto> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasksByProjectPages", key = "#projectId")
    public Page<TaskDto> getTasksByProjectId(Long projectId, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return taskRepository.findByProjectId(projectId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "tasksByDeveloperPages", key = "#developerId")
    public Page<TaskDto> getTasksByDeveloperId(Long developerId, Pageable pageable) {
        if (!developerRepository.existsById(developerId)) {
            throw new ResourceNotFoundException("Developer", "id", developerId);
        }
        return taskRepository.findByDeveloperId(developerId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> findTasksByDeveloper(Long developerId, Pageable pageable) {
        if (!developerRepository.existsById(developerId)) {
            throw new ResourceNotFoundException("Developer", "id", developerId);
        }
        return taskRepository.findByDeveloperId(developerId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("overdueTasksPage")
    public Page<TaskDto> getOverdueTasks(Pageable pageable) {
        return taskRepository.findOverdueTasks(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskStatusCountsByProject", key = "#projectId")
    public List<TaskStatusCountDto> getTaskCountsByStatusForProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return taskRepository.countTasksByStatusForProject(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("taskStatusCountsOverall")
    public List<TaskStatusCountDto> getTaskCountsByStatusOverall() {
        return taskRepository.countTasksByStatusOverall();
    }

    @Override
    @Transactional
    @Caching(put = {
        @CachePut(value = "tasks", key = "#taskId")
    }, evict = {
        @CacheEvict(value = "tasksPage", allEntries = true),
        @CacheEvict(value = "tasksByProjectPages", allEntries = true),
        @CacheEvict(value = "tasksByDeveloperPages", allEntries = true),
        @CacheEvict(value = "overdueTasksPage", allEntries = true),
        @CacheEvict(value = "taskStatusCountsByProject", allEntries = true),
        @CacheEvict(value = "taskStatusCountsOverall", allEntries = true)
    })
    public TaskDto updateTask(Long taskId, TaskRequestDto taskRequestDto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        Project project = projectRepository.findById(taskRequestDto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", taskRequestDto.getProjectId()));

        Developer developer = null;
        if (taskRequestDto.getDeveloperId() != null) {
            developer = developerRepository.findById(taskRequestDto.getDeveloperId())
                    .orElseThrow(() -> new ResourceNotFoundException("Developer", "id", taskRequestDto.getDeveloperId()));
        }

        task.setTitle(taskRequestDto.getTitle());
        task.setDescription(taskRequestDto.getDescription());
        task.setStatus(taskRequestDto.getStatus());
        task.setDueDate(taskRequestDto.getDueDate());
        task.setProject(project);
        task.setDeveloper(developer);

        Task updatedTask = taskRepository.save(task);
        auditLogService.logAction("Task", updatedTask.getId(), ActionType.UPDATED, "SYSTEM", "Task updated: " + updatedTask.getTitle());
        return mapToDto(updatedTask);
    }

    @Override
    @Transactional
    @Caching(put = {
        @CachePut(value = "tasks", key = "#taskId")
    }, evict = {
        @CacheEvict(value = "tasksPage", allEntries = true),
        @CacheEvict(value = "tasksByProjectPages", allEntries = true),
        @CacheEvict(value = "tasksByDeveloperPages", allEntries = true),
        @CacheEvict(value = "overdueTasksPage", allEntries = true),
        @CacheEvict(value = "taskStatusCountsByProject", key = "#result.project.id", condition="#result != null && #result.project != null"),
        @CacheEvict(value = "taskStatusCountsOverall", allEntries = true)
    })
    public TaskDto assignTaskToDeveloper(Long taskId, Long developerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", "id", developerId));

        task.setDeveloper(developer);
        Task updatedTask = taskRepository.save(task);
        auditLogService.logAction("Task", updatedTask.getId(), ActionType.ASSIGNED, "SYSTEM",
                String.format("Task '%s' assigned to developer '%s'", updatedTask.getTitle(), developer.getName()));
        return mapToDto(updatedTask);
    }

    @Override
    @Transactional
    @Caching(put = {
        @CachePut(value = "tasks", key = "#taskId")
    }, evict = {
        @CacheEvict(value = "tasksPage", allEntries = true),
        @CacheEvict(value = "tasksByProjectPages", allEntries = true),
        @CacheEvict(value = "tasksByDeveloperPages", allEntries = true),
        @CacheEvict(value = "overdueTasksPage", allEntries = true),
        @CacheEvict(value = "taskStatusCountsByProject", key = "#result.project.id", condition="#result != null && #result.project != null"),
        @CacheEvict(value = "taskStatusCountsOverall", allEntries = true)
    })
    public TaskDto unassignTaskFromDeveloper(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        String oldDeveloperName = task.getDeveloper() != null ? task.getDeveloper().getName() : "N/A";
        task.setDeveloper(null);
        Task updatedTask = taskRepository.save(task);
        auditLogService.logAction("Task", updatedTask.getId(), ActionType.UNASSIGNED, "SYSTEM",
                String.format("Task '%s' unassigned from developer '%s'", updatedTask.getTitle(), oldDeveloperName));
        return mapToDto(updatedTask);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "tasks", key = "#taskId"),
        @CacheEvict(value = "tasksPage", allEntries = true),
        @CacheEvict(value = "tasksByProjectPages", allEntries = true),
        @CacheEvict(value = "tasksByDeveloperPages", allEntries = true),
        @CacheEvict(value = "overdueTasksPage", allEntries = true),
        @CacheEvict(value = "taskStatusCountsByProject", allEntries = true),
        @CacheEvict(value = "taskStatusCountsOverall", allEntries = true)
    })
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        auditLogService.logAction("Task", taskId, ActionType.DELETED, "SYSTEM", "Task deleted: " + task.getTitle());
        taskRepository.deleteById(taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findTasksByProjectId(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> findOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findByDueDateBeforeAndStatusNotIn(
                LocalDate.now(),
                Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED)
        );
        return overdueTasks.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findOverdueTasksForNotification() {
        return taskRepository.findByDueDateBeforeAndStatusNotIn(
                LocalDate.now(),
                Arrays.asList(TaskStatus.COMPLETED, TaskStatus.CANCELLED)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksForCurrentUser(Pageable pageable) {
        UserPrincipal currentUser = SecurityUtil.getCurrentUserPrincipal()
                .orElseThrow(() -> new RuntimeException("No authenticated user found"));
        
        // Find the developer record that corresponds to the current user
        // This assumes that the User entity has a relationship with Developer
        // or that the developer ID is stored in the UserPrincipal
        Long developerId = currentUser.getId(); // Assuming the user ID matches developer ID
        
        return taskRepository.findByDeveloperId(developerId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("taskSummariesPage")
    public Page<TaskSummaryDto> getAllTaskSummaries(Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting all task summaries with pagination: {}", pageable);
            Page<Task> tasks = taskRepository.findAll(pageable);
            return tasks.map(taskMapper::toSummaryDto);
        } finally {
            sample.stop(meterRegistry.timer("service.task.getAllSummaries"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskSummariesByProjectPages", key = "#projectId")
    public Page<TaskSummaryDto> getTaskSummariesByProjectId(Long projectId, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting task summaries for project ID {}: {}", projectId, pageable);
            if (!projectRepository.existsById(projectId)) {
                throw new ResourceNotFoundException("Project", "id", projectId);
            }
            Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);
            return tasks.map(taskMapper::toSummaryDto);
        } finally {
            sample.stop(meterRegistry.timer("service.task.getSummariesByProjectId"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskSummariesByDeveloperPages", key = "#developerId")
    public Page<TaskSummaryDto> getTaskSummariesByDeveloperId(Long developerId, Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting task summaries for developer ID {}: {}", developerId, pageable);
            if (!developerRepository.existsById(developerId)) {
                throw new ResourceNotFoundException("Developer", "id", developerId);
            }
            Page<Task> tasks = taskRepository.findByDeveloperId(developerId, pageable);
            return tasks.map(taskMapper::toSummaryDto);
        } finally {
            sample.stop(meterRegistry.timer("service.task.getSummariesByDeveloperId"));
        }
    }

    // --- Helper Mapper Methods ---
    private TaskDto mapToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        if (task.getProject() != null) {
            TaskDto.ProjectReferenceDto projectRef = new TaskDto.ProjectReferenceDto();
            projectRef.setId(task.getProject().getId());
            projectRef.setName(task.getProject().getName());
            dto.setProject(projectRef);
        }

        if (task.getDeveloper() != null) {
            TaskDto.DeveloperReferenceDto devRef = new TaskDto.DeveloperReferenceDto();
            devRef.setId(task.getDeveloper().getId());
            devRef.setName(task.getDeveloper().getName());
            dto.setDeveloper(devRef);
        }
        return dto;
    }

    private Task mapToEntity(TaskRequestDto dto, Project project, Developer developer) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());
        task.setProject(project);
        task.setDeveloper(developer);
        // createdAt and updatedAt are handled by @CreationTimestamp and @UpdateTimestamp
        return task;
    }
}
