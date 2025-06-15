package com.example.clb.projecttracker.repository;

import com.example.clb.projecttracker.model.ERole;
import com.example.clb.projecttracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Page<User> findByRoles_Name(ERole role, Pageable pageable);

    Page<User> findByApprovedFalse(Pageable pageable);

    long countByApprovedFalse();

    long countByRoles_Name(ERole role);
}
