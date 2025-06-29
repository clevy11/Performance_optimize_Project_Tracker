package com.example.clb.projecttracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "developers")
@Data
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Developer name cannot be blank")
    @Size(max = 100, message = "Developer name must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Size(max = 255, message = "Skills description must  be less than 255 characters")
    private String skills; // e.g., "Java, Spring Boot, SQL"

    // One Developer can be assigned to Many Tasks
    // 'mappedBy = "developer"' indicates the 'developer' field in Task entity owns the relationship
    @OneToMany(mappedBy = "developer", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Task> assignedTasks = new HashSet<>();

    // Helper methods for managing the bidirectional relationship with Task
    public void addTask(Task task) {
        if (task != null) {
            assignedTasks.add(task);
            task.setDeveloper(this);
        }
    }

    public void removeTask(Task task) {
        if (task != null) {
            assignedTasks.remove(task);
            task.setDeveloper(null);
        }
    }
}
