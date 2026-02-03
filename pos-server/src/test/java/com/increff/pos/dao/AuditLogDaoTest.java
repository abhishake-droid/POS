package com.increff.pos.dao;

import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogDaoTest extends AbstractUnitTest {

    @Autowired
    private AuditLogDao auditLogDao;

    @Test
    void testSaveAndFindAll() {
        // Given
        AuditLogPojo log1 = new AuditLogPojo();
        log1.setOperatorEmail("user1@example.com");
        log1.setOperatorName("User 1");
        log1.setAction("LOGIN");
        log1.setTimestamp(ZonedDateTime.now());
        auditLogDao.save(log1);

        AuditLogPojo log2 = new AuditLogPojo();
        log2.setOperatorEmail("user2@example.com");
        log2.setOperatorName("User 2");
        log2.setAction("LOGOUT");
        log2.setTimestamp(ZonedDateTime.now());
        auditLogDao.save(log2);

        // When
        Page<AuditLogPojo> page = auditLogDao.findAll(PageRequest.of(0, 10));

        // Then
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testSaveAuditLog() {
        // Given
        AuditLogPojo log = new AuditLogPojo();
        log.setOperatorEmail("operator@example.com");
        log.setOperatorName("Operator");
        log.setAction("CREATE_ORDER");
        log.setTimestamp(ZonedDateTime.now());

        // When
        AuditLogPojo saved = auditLogDao.save(log);

        // Then
        assertNotNull(saved.getId());
        assertEquals("CREATE_ORDER", saved.getAction());
    }

    @Test
    void testFindByOperatorEmail() {
        // Given
        AuditLogPojo log = new AuditLogPojo();
        log.setOperatorEmail("specific@example.com");
        log.setOperatorName("Specific User");
        log.setAction("UPDATE");
        log.setTimestamp(ZonedDateTime.now());
        auditLogDao.save(log);

        // When
        List<AuditLogPojo> result = auditLogDao.findByOperatorEmail("specific@example.com");

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("specific@example.com", result.get(0).getOperatorEmail());
    }
}
