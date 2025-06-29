package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.response.UserDto;
import com.example.clb.projecttracker.model.Role;
import com.example.clb.projecttracker.model.User;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toSet()),
                user.getProvider() != null ? user.getProvider().name() : null
        );
    }
}
