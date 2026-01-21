package com.increff.pos.dto;

import com.increff.pos.api.AuditLogApi;
import com.increff.pos.helper.AuditLogHelper;
import com.increff.pos.model.data.AuditLogData;
import com.increff.pos.model.form.PageForm;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogDto {

    private final AuditLogApi auditLogApi;

    public AuditLogDto(AuditLogApi auditLogApi) {
        this.auditLogApi = auditLogApi;
    }

    public List<AuditLogData> getByOperatorEmail(String operatorEmail) {
        List<com.increff.pos.db.AuditLogPojo> pojoList = auditLogApi.getByOperatorEmail(operatorEmail);
        return AuditLogHelper.convertToDataList(pojoList);
    }

    public Page<AuditLogData> getAll(PageForm form) throws com.increff.pos.exception.ApiException {
        ValidationUtil.validatePageForm(form);
        Page<com.increff.pos.db.AuditLogPojo> pojoPage = auditLogApi.getAll(form.getPage(), form.getSize());
        return pojoPage.map(AuditLogHelper::convertToDto);
    }

    public List<AuditLogData> getAll() {
        List<com.increff.pos.db.AuditLogPojo> pojoList = auditLogApi.getAll();
        return AuditLogHelper.convertToDataList(pojoList);
    }
}
