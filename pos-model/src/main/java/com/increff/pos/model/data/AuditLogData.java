package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AuditLogData {
    private String id;
    private String operatorEmail;
    private String operatorName;
    private String action; // "LOGIN" or "LOGOUT"
    private Instant timestamp;
}
