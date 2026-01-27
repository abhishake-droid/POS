package com.increff.pos.api;

import com.increff.pos.dao.AuditLogDao;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuditLogApiImpl implements AuditLogApi {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogApiImpl.class);

    private final AuditLogDao dao;

    public AuditLogApiImpl(AuditLogDao dao) {
        this.dao = dao;
    }
    @Override
    @Transactional(rollbackFor = ApiException.class)
    public AuditLogPojo add(AuditLogPojo auditLogPojo) throws ApiException {
        logger.info("Creating audit log: {} - {}", auditLogPojo.getOperatorEmail(), auditLogPojo.getAction());
        auditLogPojo.setTimestamp(Instant.now());
        AuditLogPojo saved = dao.save(auditLogPojo);
        logger.info("Created audit log with id: {}", saved.getId());
        return saved;
    }

    @Override
    public List<AuditLogPojo> getByOperatorEmail(String operatorEmail) {
        return dao.findByOperatorEmail(operatorEmail);
    }

    @Override
    public Page<AuditLogPojo> getAll(int page, int size) {
        logger.info("Fetching audit logs page {} with size {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return dao.findAll(pageRequest);
    }

    @Override
    public List<AuditLogPojo> getAll() {
        return dao.findAll();
    }
}
