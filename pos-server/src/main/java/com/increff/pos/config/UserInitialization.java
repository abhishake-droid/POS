package com.increff.pos.config;

import com.increff.pos.api.UserApi;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserInitialization implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(UserInitialization.class);

    private final UserApi userApi;
    private final UserDao userDao;

    public UserInitialization(UserApi userApi, UserDao userDao){
        this.userApi = userApi;
        this.userDao = userDao;
    }

    @Value("${supervisor.email}")
    private String supervisorEmail;

    @Value("${supervisor.password}")
    private String supervisorPassword;

    @Value("${supervisor.name}")
    private String supervisorName;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        initializeSupervisor();
    }

    private void initializeSupervisor() {
        try {
            // Check if supervisor already exists
            UserPojo existingUser = userApi.getByEmail(supervisorEmail);
            if (existingUser != null) {
                // Update role and password if needed
                boolean needsUpdate = false;
                if (!"SUPERVISOR".equals(existingUser.getRole())) {
                    existingUser.setRole("SUPERVISOR");
                    needsUpdate = true;
                }
                if (existingUser.getPassword() == null || existingUser.getPassword().isEmpty()) {
                    existingUser.setPassword(passwordEncoder.encode(supervisorPassword));
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    userDao.save(existingUser);
                    logger.info("Updated supervisor user: {}", supervisorEmail);
                } else {
                    logger.info("Supervisor user already exists and is properly configured: {}", supervisorEmail);
                }
                return;
            }

            // Create supervisor user
            UserPojo supervisor = new UserPojo();
            supervisor.setEmail(supervisorEmail);
            supervisor.setName(supervisorName);
            supervisor.setPassword(passwordEncoder.encode(supervisorPassword));
            supervisor.setRole("SUPERVISOR");

            userApi.add(supervisor);
            logger.info("Supervisor user created successfully: {}", supervisorEmail);
        } catch (Exception e) {
            // If user already exists, that's fine - just log it
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                logger.info("Supervisor user already exists in database: {}", supervisorEmail);
            } else {
                logger.error("Failed to initialize supervisor user: {}", e.getMessage(), e);
            }
        }
    }
}
