package com.example.clb.projecttracker.listeners;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationFailureListener implements ApplicationListener<AuthorizationFailureEvent> {

    private final HttpServletRequest request;

    @Override
    public void onApplicationEvent(AuthorizationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String uri = request.getRequestURI();
        log.warn("Authorization failure for user: {} attempting to access: {}", username, uri);
    }
}
