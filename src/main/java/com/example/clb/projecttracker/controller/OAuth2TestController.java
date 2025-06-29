package com.example.clb.projecttracker.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test/oauth2")
@Tag(name = "OAuth2 Test", description = "Endpoints for testing OAuth2 authentication")
public class OAuth2TestController {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2TestController.class);

    @GetMapping("/public")
    @Operation(summary = "Public endpoint that doesn't require authentication")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Collections.singletonMap("message", "This is a public endpoint"));
    }

    @GetMapping("/user")
    @Operation(summary = "Get current OAuth2 user details")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getOAuth2User(Authentication authentication) {
        logger.info("OAuth2 User Info endpoint called by: {}", authentication != null ? authentication.getName() : "anonymous");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                    "error", "Not authenticated",
                    "timestamp", System.currentTimeMillis()
                )
            );
        }

        Object principal = authentication.getPrincipal();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "OAuth2 User Info");
        response.put("name", authentication.getName());
        response.put("authorities", authentication.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .collect(Collectors.toList()));
        
        if (principal instanceof OAuth2User oauth2User) {
            response.put("attributes", oauth2User.getAttributes());
        } else if (principal instanceof UserDetails userDetails) {
            response.put("userDetails", Map.of(
                "username", userDetails.getUsername(),
                "authorities", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList())
            ));
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user details (works with JWT or OAuth2)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        logger.info("Current user endpoint called by: {}", authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Current User Info");
        response.put("name", authentication.getName());
        response.put("authenticated", authentication.isAuthenticated());
        response.put("authorities", authentication.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .collect(Collectors.toList()));
            
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            response.put("authenticationType", "OAuth2");
            response.put("attributes", oauth2User.getAttributes());
        } else if (authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            response.put("authenticationType", "JWT");
            response.put("claims", jwt.getClaims());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/secured")
    @Operation(summary = "Test secured endpoint with OAuth2 or JWT")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> securedEndpoint(@AuthenticationPrincipal Object principal) {
        String userType = principal != null ? principal.getClass().getSimpleName() : "anonymous";
        return ResponseEntity.ok(Collections.singletonMap("message", 
            "This is a secured endpoint. Authenticated as: " + userType));
    }
}
