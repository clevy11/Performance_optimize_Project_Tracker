package com.example.clb.projecttracker.repository;

import com.example.clb.projecttracker.dto.DeveloperPerformanceDto;
import com.example.clb.projecttracker.model.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Long> {
    Optional<Developer> findByEmail(String email);

    // Query to find top N developers by the number of completed tasks
    @Query("SELECT new com.example.clb.projecttracker.dto.DeveloperPerformanceDto(d, COUNT(t.id)) " +
           "FROM Developer d JOIN d.assignedTasks t " +
           "WHERE t.status = com.example.clb.projecttracker.model.enums.TaskStatus.COMPLETED " +
           "GROUP BY d " +
           "ORDER BY COUNT(t.id) DESC")
    List<DeveloperPerformanceDto> findTopDevelopersByCompletedTasks(Pageable pageable); // Pageable for N

}
