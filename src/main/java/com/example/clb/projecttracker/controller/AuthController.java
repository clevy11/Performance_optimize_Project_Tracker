package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.request.LoginRequest;
import com.example.clb.projecttracker.dto.request.SignUpRequest;
import com.example.clb.projecttracker.dto.response.JwtResponse;
import com.example.clb.projecttracker.dto.response.MessageResponse;
import com.example.clb.projecttracker.model.AuthProvider;
import com.example.clb.projecttracker.model.ERole;
import com.example.clb.projecttracker.model.Role;
import com.example.clb.projecttracker.model.User;
import com.example.clb.projecttracker.repository.RoleRepository;
import com.example.clb.projecttracker.repository.UserRepository;
import com.example.clb.projecttracker.security.jwt.JwtUtils;
import com.example.clb.projecttracker.security.services.UserDetailsImpl;
import com.example.clb.projecttracker.dto.request.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));
        
        user.setProvider(AuthProvider.LOCAL);
        user.setActive(true);
        user.setApproved(true); // Local registrations are auto-approved

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(contractorRole);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "manager":
                        Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(managerRole);
                        break;
                    case "developer":
                        Role developerRole = roleRepository.findByName(ERole.ROLE_DEVELOPER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(developerRole);
                        break;
                    case "contractor":
                        Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(contractorRole);
                        break;
                    default:
                        Role defaultRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(defaultRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user details", 
               description = "Returns current logged-in user details")
    public ResponseEntity<UserDetailsImpl> getCurrentUser(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/oauth2/success")
    @Operation(summary = "OAuth2 login success", 
               description = "Redirect endpoint after successful OAuth2 login to generate JWT")
    public ResponseEntity<?> oauth2LoginSuccess(@RequestParam String token) {
        // This endpoint is called by OAuth2AuthenticationSuccessHandler
        // The token parameter contains the JWT generated for the OAuth2 user
        return ResponseEntity.ok(new MessageResponse("OAuth2 login successful. Token: " + token));
    }

    @GetMapping("/test/users")
    @Operation(summary = "Test endpoint to check saved users", 
               description = "Returns all users for debugging OAuth2 registration")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users.stream().map(user -> 
            Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "provider", user.getProvider(),
                "active", user.getActive(),
                "approved", user.getApproved(),
                "roles", user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList())
            )
        ).collect(Collectors.toList()));
    }

    @GetMapping("/login/google")
    @Operation(summary = "Initiate Google OAuth2 login",
               description = "Redirects the user to Google's authentication page to start the OAuth2 login flow.")
    public RedirectView redirectToGoogleLogin() {
        return new RedirectView("/oauth2/authorize/google");
    }

    @PostMapping("/test/create-oauth2-user")
    @Operation(summary = "Test endpoint to manually create OAuth2 user", 
               description = "Manually creates an OAuth2 user for testing")
    public ResponseEntity<?> createTestOAuth2User(@RequestParam String email, @RequestParam String name) {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("User with email " + email + " already exists"));
            }

            User user = new User();
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId("test_" + System.currentTimeMillis());
            user.setUsername(name);
            user.setEmail(email);
            user.setPassword(null);
            user.setActive(true);
            user.setApproved(false);

            Set<Role> roles = new HashSet<>();
            Role contractorRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                    .orElseThrow(() -> new RuntimeException("Error: CONTRACTOR role is not found."));
            roles.add(contractorRole);
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Test OAuth2 user created successfully",
                "user", Map.of(
                    "id", savedUser.getId(),
                    "username", savedUser.getUsername(),
                    "email", savedUser.getEmail(),
                    "provider", savedUser.getProvider(),
                    "approved", savedUser.getApproved()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new MessageResponse("Error creating test user: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Refresh JWT token", 
               description = "Generates a new JWT token using current valid token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                // Generate new JWT token for authenticated user
                String jwt = jwtUtils.generateJwtToken(authentication);
                
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toList());

                return ResponseEntity.ok(new JwtResponse(jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
            } else {
                return ResponseEntity.status(401)
                        .body(new MessageResponse("Error: Unauthorized - please login again"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new MessageResponse("Error: Token refresh failed - " + e.getMessage()));
        }
    }

    @GetMapping("/debug/oauth2-status")
    @Operation(summary = "Debug OAuth2 status", 
               description = "Shows OAuth2 configuration status for debugging")
    public ResponseEntity<?> debugOAuth2Status() {
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", new Date());
        status.put("message", "OAuth2 Debug Information");
        status.put("oauth2_endpoints", Map.of(
            "authorization", "/oauth2/authorize/google",
            "callback", "/oauth2/callback/google",
            "swagger_redirect", "/swagger-ui/oauth2-redirect.html"
        ));
        status.put("test_steps", Arrays.asList(
            "1. Open http://localhost:8080/swagger-ui.html",
            "2. Click 'Authorize' button",
            "3. Select 'googleOAuth2'", 
            "4. Complete Google login",
            "5. Check console for OAuth2 flow logs",
            "6. Verify user saved with /auth/test/users"
        ));
        status.put("expected_behavior", "OAuth2 should validate emails with Google before saving users");
        status.put("console_logs_to_watch", Arrays.asList(
            "🔐 Processing OAuth2 User Authentication",
            "📧 Email validation messages",
            "💾 Saving OAuth2 user to database",
            "🎫 JWT Token Generated"
        ));
        return ResponseEntity.ok(status);
    }

    @GetMapping("/debug/environment")
    @Operation(summary = "Debug environment variables", 
               description = "Check if OAuth2 environment variables are set (without exposing secrets)")
    public ResponseEntity<?> debugEnvironment() {
        Map<String, Object> env = new HashMap<>();
        env.put("timestamp", new Date());
        
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        
        env.put("google_client_id_set", clientId != null && !clientId.isEmpty());
        env.put("google_client_secret_set", clientSecret != null && !clientSecret.isEmpty());
        env.put("client_id_length", clientId != null ? clientId.length() : 0);
        env.put("client_secret_length", clientSecret != null ? clientSecret.length() : 0);
        
        if (clientId != null && clientId.length() > 10) {
            env.put("client_id_preview", clientId.substring(0, 10) + "...");
        }
        
        env.put("status", (clientId != null && clientSecret != null) ? "✅ Ready" : "❌ Missing credentials");
        env.put("note", "Set environment variables: GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET");
        
        return ResponseEntity.ok(env);
    }

    @GetMapping("/oauth2/direct-login")
    @Operation(summary = "Direct OAuth2 login link", 
               description = "Direct link to Google OAuth2 - use this instead of Swagger UI OAuth2")
    public ResponseEntity<?> directOAuth2Login() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Use this direct OAuth2 login URL");
        response.put("oauth2_login_url", "http://localhost:8080/oauth2/authorization/google");
        response.put("instructions", Arrays.asList(
            "1. Click the OAuth2 login URL above",
            "2. You'll be redirected to Google login",
            "3. After login, you'll be redirected back", 
            "4. Check console logs for user creation",
            "5. Verify user saved with /auth/test/users"
        ));
        response.put("note", "This bypasses Swagger UI and uses the direct OAuth2 flow");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug/database")
    @Operation(summary = "Debug database connection", 
               description = "Test if database is working and show user counts")
    public ResponseEntity<?> debugDatabase() {
        Map<String, Object> dbInfo = new HashMap<>();
        dbInfo.put("timestamp", new Date());
        
        try {
            // Test user repository
            long userCount = userRepository.count();
            dbInfo.put("total_users", userCount);
            dbInfo.put("user_repository_working", true);
            
            // Test role repository  
            long roleCount = roleRepository.count();
            dbInfo.put("total_roles", roleCount);
            dbInfo.put("role_repository_working", true);
            
            // Get sample users
            List<User> users = userRepository.findAll();
            dbInfo.put("sample_users", users.stream().limit(5).map(user -> 
                Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "provider", user.getProvider().name(),
                    "active", user.getActive(),
                    "approved", user.getApproved()
                )
            ).collect(Collectors.toList()));
            
            dbInfo.put("status", "✅ Database connection working");
            
        } catch (Exception e) {
            dbInfo.put("status", "❌ Database connection failed");
            dbInfo.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(dbInfo);
    }
}
