package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.dto.response.UserDto;
import com.example.clb.projecttracker.dto.AdminDashboardDto;
import com.example.clb.projecttracker.exception.ResourceNotFoundException;
import com.example.clb.projecttracker.model.ERole;
import com.example.clb.projecttracker.model.Role;
import com.example.clb.projecttracker.model.User;
import com.example.clb.projecttracker.repository.RoleRepository;
import com.example.clb.projecttracker.repository.UserRepository;
import com.example.clb.projecttracker.repository.ProjectRepository;
import com.example.clb.projecttracker.repository.TaskRepository;
import com.example.clb.projecttracker.service.UserMapper;
import com.example.clb.projecttracker.service.UserService;
import com.example.clb.projecttracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Override
    public Optional<UserDto> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .map(UserPrincipal::getId)
                .flatMap(userRepository::findById)
                .map(userMapper::toDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto updateUserRoles(Long userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            ERole eRole = ERole.valueOf(roleName);
            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
            roles.add(role);
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto approveContractor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setApproved(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(false);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public Page<UserDto> getUsersByRole(String roleName, Pageable pageable) {
        ERole eRole = ERole.valueOf(roleName);
        return userRepository.findByRoles_Name(eRole, pageable).map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> getPendingApprovalUsers(Pageable pageable) {
        return userRepository.findByApprovedFalse(pageable).map(userMapper::toDto);
    }

    @Override
    public AdminDashboardDto getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long pendingApprovalUsers = userRepository.countByApprovedFalse();
        
        long contractorUsers = userRepository.countByRoles_Name(ERole.ROLE_CONTRACTOR);
        long adminUsers = userRepository.countByRoles_Name(ERole.ROLE_ADMIN);
        long managerUsers = userRepository.countByRoles_Name(ERole.ROLE_MANAGER);
        long developerUsers = userRepository.countByRoles_Name(ERole.ROLE_DEVELOPER);

        // Placeholder values - these could be implemented with more complex queries
        long overdueTasksCount = 0;
        long completedTasksCount = 0;
        long activeProjectsCount = totalProjects;

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .pendingApprovalUsers(pendingApprovalUsers)
                .contractorUsers(contractorUsers)
                .adminUsers(adminUsers)
                .managerUsers(managerUsers)
                .developerUsers(developerUsers)
                .overdueTasksCount(overdueTasksCount)
                .completedTasksCount(completedTasksCount)
                .activeProjectsCount(activeProjectsCount)
                .build();
    }
}
