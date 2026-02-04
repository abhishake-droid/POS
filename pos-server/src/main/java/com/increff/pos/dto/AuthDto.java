package com.increff.pos.dto;

import com.increff.pos.api.UserApi;
import com.increff.pos.api.AuditLogApi;
import com.increff.pos.db.UserPojo;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuthData;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class AuthDto {

    @Autowired
    private UserApi userApi;

    @Autowired
    private AuditLogApi auditLogApi;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String SERVER_INSTANCE_ID = java.util.UUID.randomUUID().toString();

    @Value("${supervisor.email}")
    private String supervisorEmail;

    public AuthData login(LoginForm loginForm) throws ApiException {
        ValidationUtil.validate(loginForm);

        String email = loginForm.getEmail().trim();
        boolean isSupervisorEmail = supervisorEmail != null && supervisorEmail.equalsIgnoreCase(email);

        UserPojo user = userApi.getByEmail(email);
        if (user == null) {
            String errorMsg = isSupervisorEmail ? "Invalid email or password"
                    : "Operator not found. Please contact supervisor.";
            throw new ApiException(errorMsg);
        }

        if (user.getPassword() == null || !passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid email or password");
        }

        String role = isSupervisorEmail ? (user.getRole() != null ? user.getRole() : "USER") : "USER";
        if (isSupervisorEmail && !"SUPERVISOR".equals(role)) {
            throw new ApiException("Invalid email or password");
        }

        String token = Base64.getEncoder()
                .encodeToString((user.getEmail() + ":" + role + ":" + SERVER_INSTANCE_ID).getBytes());
        AuthData authData = new AuthData();
        authData.setToken(token);
        authData.setEmail(user.getEmail());
        authData.setName(user.getName());
        authData.setRole(role);

        logActivity(user.getEmail(), user.getName(), "LOGIN");
        return authData;
    }

    public AuthData validateToken(String token) throws ApiException {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length != 3) {
                throw new ApiException("Invalid token");
            }

            String email = parts[0];
            String role = parts[1];
            String serverId = parts[2];

            if (!SERVER_INSTANCE_ID.equals(serverId)) {
                throw new ApiException("Session expired or server restarted. Please login again.");
            }

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
        } catch (ApiException e) {
            throw e;
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
            auditLog.setTimestamp(java.time.ZonedDateTime.now());
            auditLogApi.add(auditLog);
        } catch (Exception e) {
            // Silently ignore audit log errors to prevent disrupting auth flow
        }
    }
}
