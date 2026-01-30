package com.increff.pos.api;

import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AuditLogApi {
    AuditLogPojo add(AuditLogPojo auditLogPojo) throws ApiException;

    List<AuditLogPojo> getByOperatorEmail(String operatorEmail);

    Page<AuditLogPojo> getAll(PageForm form);

    List<AuditLogPojo> getAll();
}
