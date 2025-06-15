package com.example.clb.projecttracker.security.oauth2;

import com.example.clb.projecttracker.exception.OAuth2AuthenticationProcessingException;
import com.example.clb.projecttracker.model.AuthProvider;
import com.example.clb.projecttracker.model.ERole;
import com.example.clb.projecttracker.model.Role;
import com.example.clb.projecttracker.model.User;
import com.example.clb.projecttracker.repository.RoleRepository;
import com.example.clb.projecttracker.repository.UserRepository;
import com.example.clb.projecttracker.security.oauth2.user.OAuth2UserInfo;
import com.example.clb.projecttracker.security.oauth2.user.OAuth2UserInfoFactory;
import com.example.clb.projecttracker.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        logger.info("🚀 OAuth2UserService.loadUser() called!");
        logger.info("📋 Client Registration ID: {}", oAuth2UserRequest.getClientRegistration().getRegistrationId());
        logger.info("📋 Client Name: {}", oAuth2UserRequest.getClientRegistration().getClientName());
        
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        logger.info("✅ Successfully loaded OAuth2User from provider");
        logger.info("📧 OAuth2User attributes: {}", oAuth2User.getAttributes());

        try {
            OAuth2User result = processOAuth2User(oAuth2UserRequest, oAuth2User);
            logger.info("🎉 processOAuth2User completed successfully!");
            return result;
        } catch (AuthenticationException ex) {
            logger.error("❌ AuthenticationException in OAuth2UserService: {}", ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error("❌ General Exception in OAuth2UserService: {}", ex.getMessage(), ex);
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        logger.info("🔐 Processing OAuth2 User Authentication");
        logger.info("📋 Provider: {}", oAuth2UserRequest.getClientRegistration().getRegistrationId());
        
        try {
            OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(), 
                oAuth2User.getAttributes()
            );
            
            logger.info("👤 OAuth2 User Info extracted:");
            logger.info("   📧 Email: {}", oAuth2UserInfo.getEmail());
            logger.info("   🏷️  Name: {}", oAuth2UserInfo.getName());
            logger.info("   🆔 ID: {}", oAuth2UserInfo.getId());
            
            // Validate that we have a valid email from Google
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
                logger.error("❌ No email found from OAuth2 provider - authentication failed");
                throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider. Please ensure your Google account has a verified email.");
        }

            // Validate email format (additional security)
            if (!oAuth2UserInfo.getEmail().contains("@") || !oAuth2UserInfo.getEmail().contains(".")) {
                logger.error("❌ Invalid email format from OAuth2 provider: {}", oAuth2UserInfo.getEmail());
                throw new OAuth2AuthenticationProcessingException("Invalid email format from OAuth2 provider.");
            }

            logger.info("🔍 Searching database for existing user with email: {}", oAuth2UserInfo.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
            
        User user;
        if (userOptional.isPresent()) {
                logger.info("👥 EXISTING USER FOUND - Will update existing user");
            user = userOptional.get();
                logger.info("   🆔 Existing User ID: {}", user.getId());
                logger.info("   👤 Existing Username: {}", user.getUsername());
                logger.info("   🏢 Existing Provider: {}", user.getProvider());
                
                // Check if provider matches
                AuthProvider currentProvider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
                if (!user.getProvider().equals(currentProvider)) {
                    logger.error("❌ Provider mismatch: User registered with {} but trying to login with {}", 
                        user.getProvider(), currentProvider);
                    throw new OAuth2AuthenticationProcessingException("Account already exists with " +
                            user.getProvider() + " provider. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
                logger.info("🆕 NEW USER - No existing user found, will create new user");
                logger.info("🚀 About to call registerNewUser() method");
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
                logger.info("✅ registerNewUser() completed, returned user with ID: {}", user != null ? user.getId() : "NULL");
            }

            if (user == null) {
                logger.error("❌ CRITICAL: User object is null after registration/update!");
                throw new OAuth2AuthenticationProcessingException("Failed to create or update user");
            }

            logger.info("✅ Building UserDetailsImpl for user ID: {}", user.getId());
            UserDetailsImpl userDetails = UserDetailsImpl.build(user, oAuth2User.getAttributes());
            logger.info("🎉 OAuth2 user processing completed successfully!");
            
            return userDetails;
            
        } catch (Exception e) {
            logger.error("❌ Exception in processOAuth2User: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        try {
            logger.info("🆕 Starting new OAuth2 user registration");
            logger.info("   📧 Email from OAuth2: {}", oAuth2UserInfo.getEmail());
            logger.info("   👤 Name from OAuth2: {}", oAuth2UserInfo.getName());
            logger.info("   🏢 Provider: {}", oAuth2UserRequest.getClientRegistration().getRegistrationId());

            User user = new User();

            // Set OAuth2 provider
            AuthProvider provider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
            user.setProvider(provider);
            user.setProviderId(oAuth2UserInfo.getId());

            // Handle username - use name from Google, fallback to email part if needed
            String username = oAuth2UserInfo.getName();
            if (username == null || username.trim().isEmpty()) {
                username = oAuth2UserInfo.getEmail().split("@")[0];
                logger.info("   📝 Using email-based username: {}", username);
            }

            // Ensure username is unique
            String baseUsername = username;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + "_" + counter++;
                logger.info("   🔄 Username already exists, trying: {}", username);
            }
            user.setUsername(username);
            
            user.setEmail(oAuth2UserInfo.getEmail());
            user.setPassword(null); // OAuth2 users don't have passwords
            user.setActive(true);
            user.setApproved(true);

            // Assign default role
            logger.info("➕ Assigning default role 'ROLE_CONTRACTOR' to new user");
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_CONTRACTOR)
                    .orElseThrow(() -> {
                        logger.error("❌ ROLE_CONTRACTOR not found in database!");
                        return new RuntimeException("Error: Role ROLE_CONTRACTOR is not found.");
                    });
            roles.add(userRole);
            user.setRoles(roles);

            logger.info("💾 Saving new user to database...");
            try {
                User savedUser = userRepository.saveAndFlush(user);
                logger.info("✅ User successfully saved! ID: {}", savedUser.getId());
                return savedUser;
            } catch (Exception e) {
                logger.error("❌ Failed to save user to database!", e);
                throw new OAuth2AuthenticationProcessingException("Failed to save user: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("❌ Exception during user registration", e);
            throw new OAuth2AuthenticationProcessingException("Failed to register user: " + e.getMessage());
        }
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        logger.info("🔄 Updating existing OAuth2 user: {}", existingUser.getEmail());
        
        // Update username if it has changed (but keep it unique)
        String newUsername = oAuth2UserInfo.getName();
        if (StringUtils.hasText(newUsername) && !newUsername.equals(existingUser.getUsername())) {
            // Check if new username is already taken by another user
            if (userRepository.existsByUsername(newUsername) && 
                !newUsername.equals(existingUser.getUsername())) {
                newUsername = newUsername + "_" + oAuth2UserInfo.getId();
            }
            existingUser.setUsername(newUsername);
            logger.info("   📝 Updated username to: {}", newUsername);
        }
        
        // Update provider ID if needed
        if (StringUtils.hasText(oAuth2UserInfo.getId()) && 
            !oAuth2UserInfo.getId().equals(existingUser.getProviderId())) {
            existingUser.setProviderId(oAuth2UserInfo.getId());
            logger.info("   🆔 Updated provider ID");
        }
        
        User updatedUser = userRepository.saveAndFlush(existingUser);
        logger.info("✅ Existing OAuth2 user updated successfully");
        
        return updatedUser;
    }
}
