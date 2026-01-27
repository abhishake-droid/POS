package com.increff.pos.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

import java.time.Instant;

@Data
@Document(collection = "audit_logs")
public class AuditLogPojo extends AbstractPojo {
    @Field("operatorEmail")
    private String operatorEmail;
    
    @Field("operatorName")
    private String operatorName;
    
    @Field("action")
    private String action; // "LOGIN" or "LOGOUT"
    
    @Field("timestamp")
    private Instant timestamp;
}
