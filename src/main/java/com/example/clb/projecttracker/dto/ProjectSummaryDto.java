package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * A lightweight DTO that includes only essential project information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDto {
    private Long id;
    private String name;
    private ProjectStatus status;
    private LocalDate deadline;
    private int taskCount; // Total number of tasks
} 