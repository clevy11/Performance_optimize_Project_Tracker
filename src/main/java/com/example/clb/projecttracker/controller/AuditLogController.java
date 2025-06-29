package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.document.AuditLog;
import com.example.clb.projecttracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/all")
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
            @PageableDefault(size = 20, sort = "timestamp,desc") Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping(params = "entityType")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntityType(
            @RequestParam String entityType,
            @PageableDefault(size = 20, sort = "timestamp,desc") Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByEntityType(entityType, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping(params = {"entityType", "entityId"})
    public ResponseEntity<Page<AuditLog>> getAuditLogsByEntityId(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @PageableDefault(size = 20, sort = "timestamp,desc") Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByEntityId(entityType, entityId, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping(params = "userId")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUserId(
            @RequestParam String userId,
            @PageableDefault(size = 20, sort = "timestamp,desc") Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogService.getAuditLogsByUserId(userId, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}
