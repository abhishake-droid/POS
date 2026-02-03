package com.increff.pos.dto;

import com.increff.pos.api.AuditLogApi;
import com.increff.pos.helper.AuditLogHelper;
import com.increff.pos.model.data.AuditLogData;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogDto {

    @Autowired
    private AuditLogApi auditLogApi;

    public List<AuditLogData> getByOperatorEmail(String operatorEmail) {
        List<AuditLogPojo> pojoList = auditLogApi.getByOperatorEmail(operatorEmail);
        return AuditLogHelper.convertToDataList(pojoList);
    }

    public Page<AuditLogData> getAll(PageForm form) throws ApiException {
        ValidationUtil.validate(form);
        Page<AuditLogPojo> pojoPage = auditLogApi.getAll(form);
        return pojoPage.map(AuditLogHelper::convertToData);
    }

    public List<AuditLogData> getAll() {
        List<AuditLogPojo> pojoList = auditLogApi.getAll();
        return AuditLogHelper.convertToDataList(pojoList);
    }
}
