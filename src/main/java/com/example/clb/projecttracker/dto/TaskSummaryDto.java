package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Lightweight DTO for task data with only essential fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryDto {
    private Long id;
    private String title;
    private TaskStatus status;
    private LocalDate dueDate;
    
    // Reference IDs only (no nested objects)
    private Long projectId;
    private String projectName;
    private Long developerId;
    private String developerName;
} 