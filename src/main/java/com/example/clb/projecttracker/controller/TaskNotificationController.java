package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.scheduler.TaskNotificationScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks/notifications")
@Tag(name = "Task Notification Controller", description = "APIs for managing task notifications")
@RequiredArgsConstructor
public class TaskNotificationController {

    private final TaskNotificationScheduler taskNotificationScheduler;

    @PostMapping("/send-overdue")
    @Operation(summary = "Manually trigger sending notifications for overdue tasks",
               description = "This endpoint invokes the scheduled job that checks for overdue tasks and sends email notifications to assigned developers.")
    @ApiResponse(responseCode = "200", description = "Overdue task notification job triggered successfully")
    @ApiResponse(responseCode = "500", description = "Internal server error if the job fails to trigger or during its execution")
    public ResponseEntity<String> triggerOverdueTaskNotifications() {
        try {
            taskNotificationScheduler.sendOverdueTaskNotifications();
            return ResponseEntity.ok("Overdue task notification job has been successfully triggered.");
        } catch (Exception e) {
            // Log the exception properly in a real application
            return ResponseEntity.status(500).body("Failed to trigger overdue task notification job: " + e.getMessage());
        }
    }
}
