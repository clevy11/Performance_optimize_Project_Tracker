package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.DeveloperDto;
import com.example.clb.projecttracker.dto.DeveloperRequestDto;
import com.example.clb.projecttracker.dto.DeveloperPerformanceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeveloperService {

    DeveloperDto createDeveloper(DeveloperRequestDto developerRequestDto);

    DeveloperDto getDeveloperById(Long developerId);

    Page<DeveloperDto> getAllDevelopers(Pageable pageable);

    List<DeveloperPerformanceDto> getTopDevelopersByCompletedTasks(int limit);

    DeveloperDto updateDeveloper(Long developerId, DeveloperRequestDto developerRequestDto);

    void deleteDeveloper(Long developerId);

    // Later we can add methods like: findTopDevelopersWithMostTasks()
}
