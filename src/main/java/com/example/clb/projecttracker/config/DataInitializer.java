package com.example.clb.projecttracker.config;

import com.example.clb.projecttracker.model.AuthProvider;
import com.example.clb.projecttracker.model.ERole;
import com.example.clb.projecttracker.model.Role;
import com.example.clb.projecttracker.model.User;
import com.example.clb.projecttracker.repository.RoleRepository;
import com.example.clb.projecttracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("🚀 Starting Data Initialization...");
        try {
            initializeRoles();
            initializeDefaultAdmin();
            logger.info("✅ Data Initialization completed successfully!");
        } catch (Exception e) {
            logger.error("❌ Data Initialization failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void initializeRoles() {
        logger.info("🎭 Starting role initialization...");
        try {
            for (ERole roleName : ERole.values()) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                    logger.info("✅ Created role: {}", roleName);
                } else {
                    logger.info("ℹ️ Role already exists: {}", roleName);
                }
            }
            logger.info("✅ Role initialization completed successfully!");
        } catch (Exception e) {
            logger.error("❌ Failed to initialize roles: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void initializeDefaultAdmin() {
        logger.info("👑 Checking for default admin user...");
        
        if (!userRepository.existsByUsername("admin")) {
            logger.info("🆕 Creating default admin user...");
            
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("728728"));
            admin.setProvider(AuthProvider.LOCAL);
            admin.setActive(true);
            admin.setApproved(true);

            Set<Role> adminRoles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Admin role is not found."));
            adminRoles.add(adminRole);
            admin.setRoles(adminRoles);

            User savedAdmin = userRepository.save(admin);
            
            logger.info("✅ Default admin user created successfully!");
            logger.info("📋 Admin User Details:");
            logger.info("   🆔 ID: {}", savedAdmin.getId());
            logger.info("   👤 Username: admin");
            logger.info("   📧 Email: admin@gmail.com");
            logger.info("   🔑 Password: 728728");
            logger.info("   🎭 Role: ADMIN");
            logger.info("   ✅ Active: {}", savedAdmin.getActive());
            logger.info("   ✅ Approved: {}", savedAdmin.getApproved());
        } else {
            logger.info("ℹ️  Default admin user already exists");
            User existingAdmin = userRepository.findByUsername("admin").orElse(null);
            if (existingAdmin != null) {
                logger.info("📋 Existing Admin Details:");
                logger.info("   🆔 ID: {}", existingAdmin.getId());
                logger.info("   👤 Username: {}", existingAdmin.getUsername());
                logger.info("   📧 Email: {}", existingAdmin.getEmail());
                logger.info("   ✅ Active: {}", existingAdmin.getActive());
                logger.info("   ✅ Approved: {}", existingAdmin.getApproved());
            }
        }
    }
}
