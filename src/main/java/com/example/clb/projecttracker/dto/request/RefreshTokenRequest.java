package com.example.clb.projecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    
    @NotBlank
    private String refreshToken;
} 