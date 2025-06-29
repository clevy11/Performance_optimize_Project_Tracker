package com.example.clb.projecttracker.dto;

import com.example.clb.projecttracker.model.enums.ProjectStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequestDto {

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 100, message = "Project name must be less than 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Project deadline cannot be null")
    @FutureOrPresent(message = "Project deadline must be in the present or future")
    private LocalDate deadline;

    @NotNull(message = "Project status cannot be null")
    private ProjectStatus status;
}
