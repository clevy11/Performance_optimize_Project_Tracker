package com.example.clb.projecttracker.security;

import com.example.clb.projecttracker.model.Task;
import com.example.clb.projecttracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("taskSecurityService")
@RequiredArgsConstructor
public class TaskSecurityService {

    private final TaskRepository taskRepository;

    public boolean isAssigneeOrAdminOrManager(Long taskId) {
        return SecurityUtil.getCurrentUserPrincipal().map(principal -> {
            if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
                return true;
            }
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null || task.getDeveloper() == null) {
                return false;
            }
            return task.getDeveloper().getId().equals(principal.getId());
        }).orElse(false);
    }

    public boolean canViewTask(Long taskId) {
        return SecurityUtil.getCurrentUserPrincipal().map(principal -> {
            // Admin and Manager can view all tasks
            if (principal.getAuthorities().stream().anyMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
                return true;
            }
            
            // Developers can only view tasks assigned to them
            if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"))) {
                Task task = taskRepository.findById(taskId).orElse(null);
                if (task == null || task.getDeveloper() == null) {
                    return false;
                }
                return task.getDeveloper().getId().equals(principal.getId());
            }
            
            // Contractors cannot view individual tasks
            return false;
        }).orElse(false);
    }

    public boolean canUpdateTask(Long taskId) {
        return SecurityUtil.getCurrentUserPrincipal().map(principal -> {
            // Admin and Manager can update all tasks
            if (principal.getAuthorities().stream().anyMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
                return true;
            }
            
            // Developers can only update tasks assigned to them
            if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"))) {
                Task task = taskRepository.findById(taskId).orElse(null);
                if (task == null || task.getDeveloper() == null) {
                    return false;
                }
                return task.getDeveloper().getId().equals(principal.getId());
            }
            
            // Contractors cannot update tasks
            return false;
        }).orElse(false);
    }
}
