package com.increff.pos.api;

import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogApiTest extends AbstractUnitTest {

    @Autowired
    private AuditLogApi auditLogApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        AuditLogPojo log = new AuditLogPojo();
        log.setOperatorEmail("test@example.com");
        log.setOperatorName("Test User");
        log.setAction("LOGIN");
        log.setTimestamp(ZonedDateTime.now());

        // When
        AuditLogPojo result = auditLogApi.add(log);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test@example.com", result.getOperatorEmail());
    }

    @Test
    void testMultipleActions() throws ApiException {
        // Given
        AuditLogPojo log1 = new AuditLogPojo();
        log1.setOperatorEmail("multi@example.com");
        log1.setOperatorName("Multi User");
        log1.setAction("CREATE_ORDER");
        log1.setTimestamp(ZonedDateTime.now());

        AuditLogPojo log2 = new AuditLogPojo();
        log2.setOperatorEmail("multi@example.com");
        log2.setOperatorName("Multi User");
        log2.setAction("UPDATE_ORDER");
        log2.setTimestamp(ZonedDateTime.now());

        // When
        AuditLogPojo result1 = auditLogApi.add(log1);
        AuditLogPojo result2 = auditLogApi.add(log2);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1.getId(), result2.getId());
    }
}
