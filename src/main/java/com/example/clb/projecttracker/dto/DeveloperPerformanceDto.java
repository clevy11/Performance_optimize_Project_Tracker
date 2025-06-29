package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.Developer;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeveloperPerformanceDto {
    private DeveloperDto developer;
    private Long completedTasksCount;

    // Constructor for JPQL query
    public DeveloperPerformanceDto(Developer developerEntity, Long completedTasksCount) {
        // Manually map Developer entity to DeveloperDto
        this.developer = new DeveloperDto(
                developerEntity.getId(),
                developerEntity.getName(),
                developerEntity.getEmail(),
                developerEntity.getSkills()
        );
        this.completedTasksCount = completedTasksCount;
    }

    // If you still need an all-args constructor for other purposes:
    public DeveloperPerformanceDto(DeveloperDto developer, Long completedTasksCount) {
        this.developer = developer;
        this.completedTasksCount = completedTasksCount;
    }
}
