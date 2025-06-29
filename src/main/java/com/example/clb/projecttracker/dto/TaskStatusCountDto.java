package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusCountDto {
    private TaskStatus status;
    private Long count;
}
