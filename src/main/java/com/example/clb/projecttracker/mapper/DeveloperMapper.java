package com.example.clb.projecttracker.mapper;

import com.example.clb.projecttracker.dto.DeveloperDto;
import com.example.clb.projecttracker.dto.DeveloperRequestDto;
import com.example.clb.projecttracker.model.Developer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeveloperMapper extends EntityMapper<DeveloperDto, Developer> {

    DeveloperDto toDto(Developer developer);
    
    Developer toEntity(DeveloperRequestDto dto);
    
    void updateEntityFromDto(DeveloperRequestDto dto, @MappingTarget Developer developer);
} 