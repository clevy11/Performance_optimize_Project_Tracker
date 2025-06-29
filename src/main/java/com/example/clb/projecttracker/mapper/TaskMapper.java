package com.example.clb.projecttracker.mapper;

import com.example.clb.projecttracker.dto.TaskDto;
import com.example.clb.projecttracker.dto.TaskRequestDto;
import com.example.clb.projecttracker.dto.TaskSummaryDto;
import com.example.clb.projecttracker.model.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ProjectMapper.class, DeveloperMapper.class})
public interface TaskMapper extends EntityMapper<TaskDto, Task> {

    @Mapping(target = "project.id", source = "project.id")
    @Mapping(target = "project.name", source = "project.name")
    @Mapping(target = "developer.id", source = "developer.id")
    @Mapping(target = "developer.name", source = "developer.name")
    TaskDto toDto(Task task);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "developer", ignore = true)
    Task toEntity(TaskRequestDto dto);
    
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "developerId", source = "developer.id")
    @Mapping(target = "developerName", source = "developer.name")
    TaskSummaryDto toSummaryDto(Task task);
    
    void updateEntityFromDto(TaskRequestDto dto, @MappingTarget Task task);
} 