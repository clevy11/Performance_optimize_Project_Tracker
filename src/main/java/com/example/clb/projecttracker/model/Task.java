package com.example.clb.projecttracker.model;

import com.example.clb.projecttracker.model.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 150, message = "Task title must be less than 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Task status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column
    private LocalDate dueDate;

    @NotNull(message = "Task must be associated with a project")
    @ManyToOne(fetch = FetchType.LAZY) // Owning side of the relationship
    @JoinColumn(name = "project_id", nullable = false)
    @ToString.Exclude // Avoid issues with toString if Project includes tasks
    @EqualsAndHashCode.Exclude // Avoid issues with equals/hashCode if Project includes tasks
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY) // Owning side of the relationship
    @JoinColumn(name = "developer_id") // Nullable by default, so task can be unassigned
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Developer developer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
