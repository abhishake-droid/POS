package com.increff.pos.model.data;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AuditLogData {
    private String id;
    private String operatorEmail;
    private String operatorName;
    private String action; // "LOGIN" or "LOGOUT"
    private ZonedDateTime timestamp;
}
