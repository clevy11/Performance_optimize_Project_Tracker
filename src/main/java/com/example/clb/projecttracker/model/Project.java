package com.example.clb.projecttracker.model;

import com.example.clb.projecttracker.model.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name cannot be blank")
    @Size(max = 100, message = "Project name must be less than 100 characters")
    @Column(nullable = false, length = 100, unique = true) // Added unique constraint for project name
    private String name;

    @Lob // For potentially large text, maps to CLOB or TEXT depending on DB
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Project deadline cannot be null")
    @Column(nullable = false)
    private LocalDate deadline;

    @NotNull(message = "Project status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    // One Project has Many Tasks
    // mappedBy = "project" indicates that the 'project' field in the Task entity owns the relationship
    // CascadeType.ALL: operations (persist, merge, remove, refresh, detach) on Project cascade to Tasks
    // orphanRemoval = true: if a Task is removed from the tasks collection, it's deleted from the DB
    // FetchType.LAZY: tasks are loaded only when explicitly accessed
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude // Avoid circular dependency in toString()
    @EqualsAndHashCode.Exclude // Avoid circular dependency in equals() and hashCode()
    private Set<Task> tasks = new HashSet<>();

    // Helper methods for managing the bidirectional relationship with Task
    // These ensure consistency on both sides of the relationship
    public void addTask(Task task) {
        if (task != null) {
            tasks.add(task);
            task.setProject(this);
        }
    }

    public void removeTask(Task task) {
        if (task != null) {
            tasks.remove(task);
            task.setProject(null);
        }
    }
}
