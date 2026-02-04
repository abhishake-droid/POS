package com.increff.pos.helper;

import com.increff.pos.model.data.AuthData;
import com.increff.pos.db.AuditLogPojo;

import java.time.ZonedDateTime;

public class AuthHelper {

    public static AuthData createAuthData(String token, String email, String name, String role) {
        AuthData authData = new AuthData();
        authData.setToken(token);
        authData.setEmail(email);
        authData.setName(name);
        authData.setRole(role);
        return authData;
    }

    public static AuditLogPojo createAuditLog(String email, String name, String action, ZonedDateTime timestamp) {
        AuditLogPojo auditLog = new AuditLogPojo();
        auditLog.setOperatorEmail(email);
        auditLog.setOperatorName(name);
        auditLog.setAction(action);
        auditLog.setTimestamp(timestamp);
        return auditLog;
    }
}
