package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectDto createProject(ProjectRequestDto projectRequestDto);

    ProjectDto getProjectById(Long projectId);

    Page<ProjectDto> getAllProjects(Pageable pageable);

    Page<ProjectDto> getProjectsWithNoTasks(Pageable pageable);

    ProjectDto updateProject(Long projectId, ProjectRequestDto projectRequestDto);

    void deleteProject(Long projectId);

    ProjectDto getProjectSummary(Long projectId);
}
