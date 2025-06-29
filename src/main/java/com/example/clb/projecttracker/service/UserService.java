package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.response.UserDto;
import com.example.clb.projecttracker.dto.AdminDashboardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserService {

    Optional<UserDto> getCurrentUser();

    List<UserDto> getAllUsers();

    Page<UserDto> getAllUsers(Pageable pageable);

    Optional<UserDto> getUserById(Long id);

    void deleteUser(Long id);

    UserDto updateUserRoles(Long userId, Set<String> roles);

    UserDto approveContractor(Long userId);

    UserDto deactivateUser(Long userId);

    UserDto activateUser(Long userId);

    Page<UserDto> getUsersByRole(String roleName, Pageable pageable);

    Page<UserDto> getPendingApprovalUsers(Pageable pageable);

    AdminDashboardDto getAdminDashboard();
}
