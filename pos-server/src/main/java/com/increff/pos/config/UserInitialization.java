package com.increff.pos.config;

import com.increff.pos.api.UserApi;
import com.increff.pos.dao.UserDao;
import com.increff.pos.db.UserPojo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserInitialization implements CommandLineRunner {

    private final UserApi userApi;
    private final UserDao userDao;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${supervisor.email}")
    private String supervisorEmail;

    @Value("${supervisor.password}")
    private String supervisorPassword;

    @Value("${supervisor.name}")
    private String supervisorName;

    public UserInitialization(UserApi userApi, UserDao userDao) {
        this.userApi = userApi;
        this.userDao = userDao;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeSupervisor();
    }

    private void initializeSupervisor() {
        try {
            UserPojo existingUser = userApi.getByEmail(supervisorEmail);
            if (existingUser != null) {
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
                }
                return;
            }

            UserPojo supervisor = new UserPojo();
            supervisor.setEmail(supervisorEmail);
            supervisor.setName(supervisorName);
            supervisor.setPassword(passwordEncoder.encode(supervisorPassword));
            supervisor.setRole("SUPERVISOR");

            userApi.add(supervisor);
        } catch (Exception e) {
            // Silently handle or log minimal error info
        }
    }
}
