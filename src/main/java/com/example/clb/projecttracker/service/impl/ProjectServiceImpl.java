package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.document.enums.ActionType;
import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import com.example.clb.projecttracker.dto.ProjectSummaryDto;
import com.example.clb.projecttracker.exception.DuplicateResourceException;
import com.example.clb.projecttracker.exception.ResourceNotFoundException;
import com.example.clb.projecttracker.mapper.ProjectMapper;
import com.example.clb.projecttracker.model.Project;
import com.example.clb.projecttracker.repository.ProjectRepository;
import com.example.clb.projecttracker.repository.TaskRepository;
import com.example.clb.projecttracker.service.AuditLogService;
import com.example.clb.projecttracker.service.ProjectService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;
    private final ProjectMapper projectMapper;
    private final MeterRegistry meterRegistry;

    @Override
    @Transactional
    @CacheEvict(value = {"projectsPage", "projectSummariesPage"}, allEntries = true)
    public ProjectDto createProject(ProjectRequestDto projectRequestDto) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            projectRepository.findByName(projectRequestDto.getName()).ifPresent(p -> {
                throw new DuplicateResourceException("Project", "name", projectRequestDto.getName());
            });
    
            Project project = projectMapper.toEntity(projectRequestDto);
            Project savedProject = projectRepository.save(project);
            
            // Log action
            auditLogService.logAction("Project", savedProject.getId(), ActionType.CREATED, "SYSTEM", 
                    "Project created: " + savedProject.getName());
                    
            return projectMapper.toDto(savedProject);
        } finally {
            sample.stop(meterRegistry.timer("service.project.create"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectDto getProjectById(Long projectId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting project with ID: {}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
            return projectMapper.toDto(project);
        } finally {
            sample.stop(meterRegistry.timer("service.project.getById"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectSummary", key = "#projectId")
    public ProjectSummaryDto getProjectSummary(Long projectId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting project summary with ID: {}", projectId);
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
                    
            ProjectSummaryDto summaryDto = projectMapper.toSummaryDto(project);
            // Get task count and populate it
            long taskCount = taskRepository.countByProjectId(projectId);
            summaryDto.setTaskCount((int)taskCount);
            
            return summaryDto;
        } finally {
            sample.stop(meterRegistry.timer("service.project.getSummary"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectsPage") // Key will be generated based on Pageable
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting all projects with pagination: {}", pageable);
            Page<Project> projects = projectRepository.findAll(pageable);
            return projects.map(projectMapper::toDto);
        } finally {
            sample.stop(meterRegistry.timer("service.project.getAllProjects"));
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectSummariesPage") 
    public Page<ProjectSummaryDto> getAllProjectSummaries(Pageable pageable) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting all project summaries with pagination: {}", pageable);
            Page<Project> projects = projectRepository.findAll(pageable);
            return projects.map(project -> {
                ProjectSummaryDto dto = projectMapper.toSummaryDto(project);
                long count = taskRepository.countByProjectId(project.getId());
                dto.setTaskCount((int)count);
                return dto;
            });
        } finally {
            sample.stop(meterRegistry.timer("service.project.getAllSummaries"));
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("projectsWithNoTasksPage")
    public Page<ProjectDto> getProjectsWithNoTasks(Pageable pageable) {
        return projectRepository.findProjectsWithNoTasks(pageable).map(projectMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable("recentProjects") 
    public List<ProjectSummaryDto> getRecentProjects(int limit) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.debug("Getting {} recent projects", limit);
            List<Project> projects = projectRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"))
            ).getContent();
            
            return projects.stream().map(project -> {
                ProjectSummaryDto dto = projectMapper.toSummaryDto(project);
                long count = taskRepository.countByProjectId(project.getId());
                dto.setTaskCount((int)count);
                return dto;
            }).collect(Collectors.toList());
        } finally {
            sample.stop(meterRegistry.timer("service.project.getRecentProjects"));
        }
    }

    @Override
    @Transactional
    @Caching(put = {
        @CachePut(value = "projects", key = "#projectId")
    }, evict = {
        @CacheEvict(value = {"projectsPage", "projectSummariesPage", "projectSummary", "recentProjects"}, 
                   allEntries = true),
        @CacheEvict(value = "projectsWithNoTasksPage", allEntries = true)
    })
    public ProjectDto updateProject(Long projectId, ProjectRequestDto projectRequestDto) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    
            // Check for name conflict if name is being changed
            if (projectRequestDto.getName() != null && !projectRequestDto.getName().equals(project.getName())) {
                projectRepository.findByName(projectRequestDto.getName()).ifPresent(p -> {
                    if (!p.getId().equals(projectId)) {
                        throw new DuplicateResourceException("Project", "name", projectRequestDto.getName());
                    }
                });
            }
    
            // Use the mapper to update entity fields from DTO
            projectMapper.updateEntityFromDto(projectRequestDto, project);
            
            Project updatedProject = projectRepository.save(project);
            
            // Log action
            auditLogService.logAction("Project", updatedProject.getId(), ActionType.UPDATED, "SYSTEM", 
                    "Project updated: " + updatedProject.getName());
                    
            return projectMapper.toDto(updatedProject);
        } finally {
            sample.stop(meterRegistry.timer("service.project.update"));
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "projects", key = "#projectId"),
        @CacheEvict(value = {"projectsPage", "projectSummariesPage", "projectSummary", "recentProjects", 
                            "projectsWithNoTasksPage"}, allEntries = true)
    })
    public void deleteProject(Long projectId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
                    
            projectRepository.delete(project);
            
            // Log action
            auditLogService.logAction("Project", projectId, ActionType.DELETED, "SYSTEM", 
                    "Project deleted: " + project.getName());
        } finally {
            sample.stop(meterRegistry.timer("service.project.delete"));
        }
    }
}
