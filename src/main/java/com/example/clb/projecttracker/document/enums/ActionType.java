package com.example.clb.projecttracker.document.enums;

public enum ActionType {
    CREATED,
    UPDATED,
    DELETED,
    ASSIGNED,    // e.g., Task assigned to Developer
    UNASSIGNED,  // e.g., Task unassigned from Developer
    VIEWED       // Optional: if we want to log read operations
}
