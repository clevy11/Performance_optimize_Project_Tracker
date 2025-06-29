package com.example.clb.projecttracker.mapper;

import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import com.example.clb.projecttracker.dto.ProjectSummaryDto;
import com.example.clb.projecttracker.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper extends EntityMapper<ProjectDto, Project> {

    ProjectDto toDto(Project project);
    
    Project toEntity(ProjectRequestDto dto);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "deadline", source = "deadline")
    @Mapping(target = "taskCount", ignore = true) // Will be populated manually in service
    ProjectSummaryDto toSummaryDto(Project project);
    
    void updateEntityFromDto(ProjectRequestDto dto, @MappingTarget Project project);
} 