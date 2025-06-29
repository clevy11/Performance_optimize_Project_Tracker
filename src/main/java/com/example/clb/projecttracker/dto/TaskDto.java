package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskDto {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProjectReferenceDto project; // Simplified reference to the project
    private DeveloperReferenceDto developer; // Simplified reference to the developer, can be null

    // Inner DTO for Project Reference
    @Data
    public static class ProjectReferenceDto {
        private Long id;
        private String name;
    }

    // Inner DTO for Developer Reference
    @Data
    public static class DeveloperReferenceDto {
        private Long id;
        private String name;
    }
}
