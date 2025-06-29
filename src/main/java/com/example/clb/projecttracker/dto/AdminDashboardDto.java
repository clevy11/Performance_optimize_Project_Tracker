package com.example.clb.projecttracker.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardDto {
    
    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long pendingApprovalUsers;
    private long contractorUsers;
    private long adminUsers;
    private long managerUsers;
    private long developerUsers;
    private long overdueTasksCount;
    private long completedTasksCount;
    private long activeProjectsCount;
} 