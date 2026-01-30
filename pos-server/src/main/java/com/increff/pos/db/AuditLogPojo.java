package com.increff.pos.db;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "audit_logs")
public class AuditLogPojo extends AbstractPojo {

    @Field("operatorEmail")
    private String operatorEmail;

    @Field("operatorName")
    private String operatorName;

    @Field("action")
    private String action;

    @Field("timestamp")
    private ZonedDateTime timestamp;
}
