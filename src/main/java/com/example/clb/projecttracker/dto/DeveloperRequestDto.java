package com.example.clb.projecttracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeveloperRequestDto {

    @NotBlank(message = "Developer name cannot be blank")
    @Size(max = 100, message = "Developer name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;

    @Size(max = 255, message = "Skills description must be less than 255 characters")
    private String skills;
}
