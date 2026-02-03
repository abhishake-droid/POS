package com.increff.pos.flow;

import com.increff.pos.api.UserApi;
import com.increff.pos.api.AuditLogApi;
import com.increff.pos.db.UserPojo;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AuthFlow {

    @Autowired
    private UserApi userApi;
    @Autowired
    private AuditLogApi auditLogApi;

    public UserPojo getUserAndLogActivity(String email, String name, String action) throws ApiException {
        UserPojo user = userApi.getByEmail(email);
        logActivity(email, name, action);
        return user;
    }

    public UserPojo getUserByEmail(String email) throws ApiException {
        return userApi.getByEmail(email);
    }

    public void logActivity(String email, String name, String action) {
        try {
            AuditLogPojo auditLog = new AuditLogPojo();
            auditLog.setOperatorEmail(email);
            auditLog.setOperatorName(name);
            auditLog.setAction(action);
            auditLog.setTimestamp(java.time.ZonedDateTime.now());
            auditLogApi.add(auditLog);
        } catch (Exception e) {
            // Silently handle logging failures
        }
    }
}
