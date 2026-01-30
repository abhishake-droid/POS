package com.increff.pos.api;

import com.increff.pos.dao.AuditLogDao;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.PageForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AuditLogApiImpl implements AuditLogApi {

    private final AuditLogDao dao;

    public AuditLogApiImpl(AuditLogDao dao) {
        this.dao = dao;
    }

    @Override
    @Transactional(rollbackFor = ApiException.class)
    public AuditLogPojo add(AuditLogPojo auditLogPojo) throws ApiException {
        auditLogPojo.setTimestamp(ZonedDateTime.now());
        return dao.save(auditLogPojo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogPojo> getByOperatorEmail(String operatorEmail) {
        return dao.findByOperatorEmail(operatorEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogPojo> getAll(PageForm form) {
        PageRequest pageRequest = PageRequest.of(form.getPage(), form.getSize(),
                Sort.by(Sort.Direction.DESC, "timestamp"));
        return dao.findAll(pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogPojo> getAll() {
        return dao.findAll();
    }
}
