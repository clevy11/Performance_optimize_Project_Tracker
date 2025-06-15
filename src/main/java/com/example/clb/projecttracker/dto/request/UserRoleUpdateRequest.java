package com.example.clb.projecttracker.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UserRoleUpdateRequest {
    
    @NotEmpty(message = "At least one role must be specified")
    private Set<String> roles;
} 