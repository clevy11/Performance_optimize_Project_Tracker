package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequestDto {

    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 150, message = "Task title must be less than 150 characters")
    private String title;

    private String description;

    @NotNull(message = "Task status cannot be null")
    private TaskStatus status;

    private LocalDate dueDate;

    @NotNull(message = "Project ID cannot be null")
    private Long projectId;

    private Long developerId; // Optional: task can be unassigned
}
