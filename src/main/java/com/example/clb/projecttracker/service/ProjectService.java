package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import com.example.clb.projecttracker.dto.ProjectSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {

    ProjectDto createProject(ProjectRequestDto projectRequestDto);

    ProjectDto getProjectById(Long projectId);

    Page<ProjectDto> getAllProjects(Pageable pageable);
    
    /**
     * Returns a paginated list of lightweight project summary DTOs 
     */
    Page<ProjectSummaryDto> getAllProjectSummaries(Pageable pageable);

    Page<ProjectDto> getProjectsWithNoTasks(Pageable pageable);

    ProjectDto updateProject(Long projectId, ProjectRequestDto projectRequestDto);

    void deleteProject(Long projectId);

    /**
     * Returns a lightweight project summary instead of full project details
     */
    ProjectSummaryDto getProjectSummary(Long projectId);
    
    /**
     * Returns a list of lightweight project summaries for dashboard views
     */
    List<ProjectSummaryDto> getRecentProjects(int limit);
}
