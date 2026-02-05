package com.increff.pos.db;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.ZonedDateTime;

@Getter
@Setter
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
