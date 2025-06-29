package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import com.example.clb.projecttracker.dto.ProjectSummaryDto;
import com.example.clb.projecttracker.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create a new project", 
               description = "Creates a new project. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectRequestDto projectRequestDto) {
        ProjectDto createdProject = projectService.createProject(projectRequestDto);
        return ResponseEntity.created(URI.create("/api/projects/" + createdProject.getId()))
                .body(createdProject);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project by ID", 
               description = "Retrieves a project by its ID. All authenticated users can view projects.")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long projectId) {
        ProjectDto projectDto = projectService.getProjectById(projectId);
        return ResponseEntity.ok(projectDto);
    }

    @GetMapping("/{projectId}/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project summary", 
               description = "Retrieves a project summary. Accessible to all authenticated users including contractors.")
    public ResponseEntity<ProjectSummaryDto> getProjectSummary(@PathVariable Long projectId) {
        ProjectSummaryDto summaryDto = projectService.getProjectSummary(projectId);
        return ResponseEntity.ok(summaryDto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all projects", 
               description = "Retrieves all projects with pagination. All authenticated users can view projects.")
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            @PageableDefault(size = 20, sort = "name,asc") Pageable pageable) {
        Page<ProjectDto> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }
    
    @GetMapping("/summaries")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all project summaries", 
               description = "Retrieves all project summaries with pagination. More efficient than loading complete projects.")
    public ResponseEntity<Page<ProjectSummaryDto>> getAllProjectSummaries(
            @PageableDefault(size = 20, sort = "name,asc") Pageable pageable) {
        Page<ProjectSummaryDto> summaries = projectService.getAllProjectSummaries(pageable);
        return ResponseEntity.ok(summaries);
    }
    
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recent projects", 
               description = "Retrieves a list of recent projects, limited by count parameter.")
    public ResponseEntity<List<ProjectSummaryDto>> getRecentProjects(
            @RequestParam(defaultValue = "5") int count) {
        List<ProjectSummaryDto> projects = projectService.getRecentProjects(count);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/no-tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get projects with no tasks", 
               description = "Retrieves projects that have no tasks assigned. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<Page<ProjectDto>> getProjectsWithNoTasks(
            @PageableDefault(size = 20, sort = "name,asc") Pageable pageable) {
        Page<ProjectDto> projects = projectService.getProjectsWithNoTasks(pageable);
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update a project", 
               description = "Updates a project. Only accessible by ADMIN or MANAGER roles.")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long projectId,
                                                  @Valid @RequestBody ProjectRequestDto projectRequestDto) {
        ProjectDto updatedProject = projectService.updateProject(projectId, projectRequestDto);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project", 
               description = "Deletes a project. Only accessible by ADMIN role.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
