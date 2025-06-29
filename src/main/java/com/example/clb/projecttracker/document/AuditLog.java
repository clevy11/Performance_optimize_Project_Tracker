package com.example.clb.projecttracker.document;

import com.example.clb.projecttracker.document.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id; // MongoDB ObjectId

    private LocalDateTime timestamp;
    private String entityType; // e.g., "Project", "Developer", "Task"
    private Long entityId;     // ID of the JPA entity
    private ActionType actionType;
    private String userId;     // User performing the action (e.g., username or ID)
    private String details;    // Can be a JSON string or a simple message

}
