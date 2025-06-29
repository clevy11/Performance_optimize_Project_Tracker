package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.AdminDashboardDto;
import com.example.clb.projecttracker.dto.response.UserDto;
import com.example.clb.projecttracker.dto.request.UserRoleUpdateRequest;
import com.example.clb.projecttracker.service.UserService;
import com.example.clb.projecttracker.service.AuditLogService;
import com.example.clb.projecttracker.document.AuditLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @GetMapping("/users")
    @Operation(summary = "Get all users", 
               description = "Retrieves all users in the system. Only accessible by ADMIN role.")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", 
               description = "Retrieves a specific user by their ID. Only accessible by ADMIN role.")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{userId}/roles")
    @Operation(summary = "Update user roles", 
               description = "Updates the roles assigned to a user. Only accessible by ADMIN role.")
    public ResponseEntity<UserDto> updateUserRoles(
            @PathVariable Long userId, 
            @Valid @RequestBody UserRoleUpdateRequest request) {
        UserDto updatedUser = userService.updateUserRoles(userId, request.getRoles());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/users/{userId}/approve")
    @Operation(summary = "Approve contractor user", 
               description = "Approves a contractor user and assigns appropriate roles. Only accessible by ADMIN role.")
    public ResponseEntity<UserDto> approveContractor(@PathVariable Long userId) {
        UserDto approvedUser = userService.approveContractor(userId);
        return ResponseEntity.ok(approvedUser);
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", 
               description = "Deletes a user from the system. Only accessible by ADMIN role.")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/deactivate")
    @Operation(summary = "Deactivate user", 
               description = "Deactivates a user account. Only accessible by ADMIN role.")
    public ResponseEntity<UserDto> deactivateUser(@PathVariable Long userId) {
        UserDto deactivatedUser = userService.deactivateUser(userId);
        return ResponseEntity.ok(deactivatedUser);
    }

    @PostMapping("/users/{userId}/activate")
    @Operation(summary = "Activate user", 
               description = "Activates a user account. Only accessible by ADMIN role.")
    public ResponseEntity<UserDto> activateUser(@PathVariable Long userId) {
        UserDto activatedUser = userService.activateUser(userId);
        return ResponseEntity.ok(activatedUser);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs", 
               description = "Retrieves audit logs with pagination. Only accessible by ADMIN role.")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @PageableDefault(size = 50, sort = "timestamp,desc") Pageable pageable,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogs(pageable, action, username);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/users/contractors")
    @Operation(summary = "Get contractor users", 
               description = "Retrieves all users with CONTRACTOR role. Only accessible by ADMIN role.")
    public ResponseEntity<Page<UserDto>> getContractorUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        Page<UserDto> contractors = userService.getUsersByRole("ROLE_CONTRACTOR", pageable);
        return ResponseEntity.ok(contractors);
    }

    @GetMapping("/users/pending-approval")
    @Operation(summary = "Get users pending approval", 
               description = "Retrieves OAuth2 users pending admin approval. Only accessible by ADMIN role.")
    public ResponseEntity<Page<UserDto>> getPendingApprovalUsers(
            @PageableDefault(size = 20, sort = "username") Pageable pageable) {
        Page<UserDto> pendingUsers = userService.getPendingApprovalUsers(pageable);
        return ResponseEntity.ok(pendingUsers);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard data", 
               description = "Retrieves summary data for admin dashboard. Only accessible by ADMIN role.")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        AdminDashboardDto dashboard = userService.getAdminDashboard();
        return ResponseEntity.ok(dashboard);
    }
} 