package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.document.AuditLog;
import com.example.clb.projecttracker.document.enums.ActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    void logAction(String entityType, Long entityId, ActionType actionType, String userId, String details);

    // Methods for retrieving logs - to be used by AuditLogController later
    Page<AuditLog> getAllAuditLogs(Pageable pageable);
    Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable);
    Page<AuditLog> getAuditLogsByEntityId(String entityType, Long entityId, Pageable pageable);
    Page<AuditLog> getAuditLogsByUserId(String userId, Pageable pageable);
    
    Page<AuditLog> getAuditLogs(Pageable pageable, String action, String username);
}
