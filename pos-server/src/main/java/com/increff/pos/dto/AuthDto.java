package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.api.AuditLogApi;
import com.increff.pos.db.UserPojo;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuthData;
import com.increff.pos.model.form.LoginForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AuthDto {

    private final UserApi userApi;
    private final AuditLogApi auditLogApi;

    public AuthDto(UserApi userApi, AuditLogApi auditLogApi) {
        this.userApi = userApi;
        this.auditLogApi = auditLogApi;
    }

    @Value("${supervisor.email}")
    private String supervisorEmail;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthData login(LoginForm loginForm) throws ApiException {
        if (loginForm.getEmail() == null || loginForm.getEmail().trim().isEmpty()) {
            throw new ApiException("Email is required");
        }

        String email = loginForm.getEmail().trim();
        
        // Check if email is supervisor email
        boolean isSupervisorEmail = supervisorEmail != null && supervisorEmail.equalsIgnoreCase(email);
        
        if (isSupervisorEmail) {
            // Supervisor login - require password
            UserPojo user = userApi.getByEmail(email);
            if (user == null) {
                throw new ApiException("Invalid email or password");
            }

            String role = user.getRole() != null ? user.getRole() : "USER";
            
            // Verify it's actually a supervisor
            if (!"SUPERVISOR".equals(role)) {
                throw new ApiException("Invalid email or password");
            }

            // Supervisor must provide password
            if (loginForm.getPassword() == null || loginForm.getPassword().trim().isEmpty()) {
                throw new ApiException("Password is required for supervisor");
            }
            if (user.getPassword() == null || !passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
                throw new ApiException("Invalid email or password");
            }

            // Generate token for supervisor
            String token = Base64.getEncoder().encodeToString((user.getEmail() + ":" + role).getBytes());
            AuthData authData = new AuthData();
            authData.setToken(token);
            authData.setEmail(user.getEmail());
            authData.setName(user.getName());
            authData.setRole(role);
            
            // Log login activity
            logActivity(user.getEmail(), user.getName(), "LOGIN");
            
            return authData;
        } else {
            // Operator login - must exist, no password required
            UserPojo user = userApi.getByEmail(email);
            if (user == null) {
                throw new ApiException("Operator not found. Please contact supervisor to create your account.");
            }

            // Force role to USER for non-supervisor emails
            String role = "USER";

            // Generate token for operator
            String token = Base64.getEncoder().encodeToString((user.getEmail() + ":" + role).getBytes());
            AuthData authData = new AuthData();
            authData.setToken(token);
            authData.setEmail(user.getEmail());
            authData.setName(user.getName());
            authData.setRole(role);
            
            // Log login activity
            logActivity(user.getEmail(), user.getName(), "LOGIN");
            
            return authData;
        }
    }

    public AuthData validateToken(String token) throws ApiException {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length != 2) {
                throw new ApiException("Invalid token");
            }

            String email = parts[0];
            String role = parts[1];

            UserPojo user = userApi.getByEmail(email);
            if (user == null) {
                throw new ApiException("User not found");
            }

            String userRole = user.getRole() != null ? user.getRole() : "USER";
            if (!userRole.equals(role)) {
                throw new ApiException("Invalid token");
            }

            AuthData authData = new AuthData();
            authData.setToken(token);
            authData.setEmail(user.getEmail());
            authData.setName(user.getName());
            authData.setRole(userRole);

            return authData;
        } catch (Exception e) {
            throw new ApiException("Invalid token");
        }
    }

    public void logLogout(String email) throws ApiException {
        UserPojo user = userApi.getByEmail(email);
        if (user != null) {
            logActivity(user.getEmail(), user.getName(), "LOGOUT");
        }
    }

    private void logActivity(String email, String name, String action) {
        try {
            AuditLogPojo auditLog = new AuditLogPojo();
            auditLog.setOperatorEmail(email);
            auditLog.setOperatorName(name);
            auditLog.setAction(action);
            auditLog.setTimestamp(java.time.Instant.now());
            auditLogApi.add(auditLog);
        } catch (Exception e) {
            // Log error
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
}
