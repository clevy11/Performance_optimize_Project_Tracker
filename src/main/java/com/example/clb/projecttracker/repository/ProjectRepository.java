package com.example.clb.projecttracker.repository;

import com.example.clb.projecttracker.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Custom query to find a project by its name (useful for uniqueness checks or retrieval)
    Optional<Project> findByName(String name);

    // Query to find projects with no tasks
    @Query("SELECT p FROM Project p WHERE p.tasks IS EMPTY")
    Page<Project> findProjectsWithNoTasks(Pageable pageable);


}
