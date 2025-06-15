package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.document.AuditLog;
import com.example.clb.projecttracker.document.enums.ActionType;
import com.example.clb.projecttracker.repository.AuditLogRepository;
import com.example.clb.projecttracker.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async // Make logging asynchronous to avoid impacting main transaction performance
    public void logAction(String entityType, Long entityId, ActionType actionType, String userId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .timestamp(LocalDateTime.now())
                .entityType(entityType)
                .entityId(entityId)
                .actionType(actionType)
                .userId(userId) // In a real app, get this from SecurityContextHolder
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByEntityId(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogsByUserId(String userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<AuditLog> getAuditLogs(Pageable pageable, String action, String username) {
        if (action != null && username != null) {
            // Filter by both action and username - would need custom repository method
            return auditLogRepository.findAll(pageable); // Simplified for now
        } else if (action != null) {
            return auditLogRepository.findByActionType(action, pageable);
        } else if (username != null) {
            return auditLogRepository.findByUserId(username, pageable);
        } else {
            return auditLogRepository.findAll(pageable);
        }
    }
}
