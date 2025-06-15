package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.document.enums.ActionType;
import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import com.example.clb.projecttracker.exception.DuplicateResourceException;
import com.example.clb.projecttracker.exception.ResourceNotFoundException;
import com.example.clb.projecttracker.model.Project;
import com.example.clb.projecttracker.repository.ProjectRepository;
import com.example.clb.projecttracker.service.AuditLogService;
import com.example.clb.projecttracker.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    @CacheEvict(value = "projectsPage", allEntries = true)
    public ProjectDto createProject(ProjectRequestDto projectRequestDto) {
        projectRepository.findByName(projectRequestDto.getName()).ifPresent(p -> {
            throw new DuplicateResourceException("Project", "name", projectRequestDto.getName());
        });

        Project project = mapToEntity(projectRequestDto);
        Project savedProject = projectRepository.save(project);
        // Log action
        auditLogService.logAction("Project", savedProject.getId(), ActionType.CREATED, "SYSTEM", "Project created: " + savedProject.getName());
        return mapToDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return mapToDto(project);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectSummary", key = "#projectId")
    public ProjectDto getProjectSummary(Long projectId) {
        // For now, project summary is the same as full project details
        // In the future, this could be enhanced to include task counts, progress, etc.
        return getProjectById(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectsPage") // Key will be generated based on Pageable
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        return projects.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("projectsWithNoTasksPage")
    public Page<ProjectDto> getProjectsWithNoTasks(Pageable pageable) {
        return projectRepository.findProjectsWithNoTasks(pageable).map(this::mapToDto);
    }

    @Override
    @Transactional
    @Caching(put = {
        @CachePut(value = "projects", key = "#projectId")
    }, evict = {
        @CacheEvict(value = "projectsPage", allEntries = true),
        @CacheEvict(value = "projectsWithNoTasksPage", allEntries = true) // Evict as project update might add/remove tasks indirectly (though less likely via this method)
    })
    public ProjectDto updateProject(Long projectId, ProjectRequestDto projectRequestDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // Check for name conflict if name is being changed
        if (projectRequestDto.getName() != null && !projectRequestDto.getName().equals(project.getName())) {
            projectRepository.findByName(projectRequestDto.getName()).ifPresent(p -> {
                if (!p.getId().equals(projectId)) {
                    throw new DuplicateResourceException("Project", "name", projectRequestDto.getName());
                }
            });
            project.setName(projectRequestDto.getName());
        }

        if (projectRequestDto.getDescription() != null) {
            project.setDescription(projectRequestDto.getDescription());
        }
        if (projectRequestDto.getDeadline() != null) {
            project.setDeadline(projectRequestDto.getDeadline());
        }
        if (projectRequestDto.getStatus() != null) {
            project.setStatus(projectRequestDto.getStatus());
        }

        Project updatedProject = projectRepository.save(project);
        // Log action
        auditLogService.logAction("Project", updatedProject.getId(), ActionType.UPDATED, "SYSTEM", "Project updated: " + updatedProject.getName());
        return mapToDto(updatedProject);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "projects", key = "#projectId"),
        @CacheEvict(value = "projectsPage", allEntries = true),
        @CacheEvict(value = "projectsWithNoTasksPage", allEntries = true) // Evict when project is deleted
    })
    public void deleteProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        // Log action before deletion
        auditLogService.logAction("Project", projectId, ActionType.DELETED, "SYSTEM", "Project deleted with ID: " + projectId);
        projectRepository.deleteById(projectId);
    }

    // --- Helper Mapper Methods ---
    private ProjectDto mapToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setDeadline(project.getDeadline());
        dto.setStatus(project.getStatus());
        return dto;
    }

    private Project mapToEntity(ProjectRequestDto dto) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setDeadline(dto.getDeadline());
        project.setStatus(dto.getStatus());
        // Tasks are managed separately, not directly set from ProjectRequestDto
        return project;
    }
}
