package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.ProjectStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectDto {

    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private ProjectStatus status;
    // We might add taskCount or a summarized list of tasks later if needed for specific views
}
