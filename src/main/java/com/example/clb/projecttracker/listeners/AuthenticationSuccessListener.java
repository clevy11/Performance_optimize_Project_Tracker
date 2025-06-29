package com.example.clb.projecttracker.listeners;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
@Slf4j
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            String username = null;
            String authType = "";
            String ipAddress = "unknown";

            // Get request if available
            Optional<HttpServletRequest> request = getCurrentHttpRequest();
            
            // Handle different types of authentication
            Object principal = authentication.getPrincipal();
            
            if (principal instanceof OAuth2User oauth2User) {
                // Handle OAuth2 login
                username = oauth2User.getAttribute("name");
                if (username == null) {
                    username = oauth2User.getAttribute("login"); // For GitHub
                }
                if (username == null) {
                    username = oauth2User.getAttribute("email");
                }
                if (username == null) {
                    username = oauth2User.getName();
                }
                authType = "OAuth2";
            } else if (principal instanceof UserDetails userDetails) {
                // Handle regular form login
                username = userDetails.getUsername();
                authType = "Form";
            } else if (principal instanceof String) {
                // Handle other cases where principal might be a String
                username = (String) principal;
                authType = "String";
            } else {
                // Fallback for any other type
                username = authentication.getName();
                authType = authentication.getClass().getSimpleName();
            }

            // Get IP address if request is available
            if (request.isPresent()) {
                ipAddress = request.get().getRemoteAddr();
            }

            log.info("Successful {} login for user: {} from IP: {}", authType, username, ipAddress);
        } catch (Exception e) {
            log.error("Error processing authentication success event", e);
        }
    }

    private Optional<HttpServletRequest> getCurrentHttpRequest() {
        try {
            return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                    .filter(ServletRequestAttributes.class::isInstance)
                    .map(ServletRequestAttributes.class::cast)
                    .map(ServletRequestAttributes::getRequest);
        } catch (IllegalStateException e) {
            log.debug("No request bound to current thread");
            return Optional.empty();
        }
    }
}
