package com.example.clb.projecttracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDto {

    private Long id;
    private String name;
    private String email;
    private String skills;
    // We might add assignedTaskCount or a summarized list of tasks later
}
