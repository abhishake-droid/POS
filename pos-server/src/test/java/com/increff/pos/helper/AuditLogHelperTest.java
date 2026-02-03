package com.increff.pos.helper;

import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.model.data.AuditLogData;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogHelperTest {

    @Test
    void testConvertToDto() {
        // Given
        AuditLogPojo pojo = new AuditLogPojo();
        pojo.setId("log123");
        pojo.setOperatorEmail("operator@example.com");
        pojo.setOperatorName("Operator Name");
        pojo.setAction("CREATE_ORDER");
        pojo.setTimestamp(ZonedDateTime.now());

        // When
        AuditLogData data = AuditLogHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertEquals("log123", data.getId());
        assertEquals("operator@example.com", data.getOperatorEmail());
        assertEquals("Operator Name", data.getOperatorName());
        assertEquals("CREATE_ORDER", data.getAction());
        assertNotNull(data.getTimestamp());
    }

    @Test
    void testConvertToDataList() {
        // Given
        AuditLogPojo log1 = new AuditLogPojo();
        log1.setId("log1");
        log1.setOperatorEmail("user1@example.com");
        log1.setOperatorName("User 1");
        log1.setAction("LOGIN");
        log1.setTimestamp(ZonedDateTime.now());

        AuditLogPojo log2 = new AuditLogPojo();
        log2.setId("log2");
        log2.setOperatorEmail("user2@example.com");
        log2.setOperatorName("User 2");
        log2.setAction("LOGOUT");
        log2.setTimestamp(ZonedDateTime.now());

        List<AuditLogPojo> pojoList = Arrays.asList(log1, log2);

        // When
        List<AuditLogData> dataList = AuditLogHelper.convertToDataList(pojoList);

        // Then
        assertNotNull(dataList);
        assertEquals(2, dataList.size());
        assertEquals("log1", dataList.get(0).getId());
        assertEquals("log2", dataList.get(1).getId());
        assertEquals("LOGIN", dataList.get(0).getAction());
        assertEquals("LOGOUT", dataList.get(1).getAction());
    }

    @Test
    void testConvertToDataList_EmptyList() {
        // Given
        List<AuditLogPojo> pojoList = Arrays.asList();

        // When
        List<AuditLogData> dataList = AuditLogHelper.convertToDataList(pojoList);

        // Then
        assertNotNull(dataList);
        assertEquals(0, dataList.size());
    }

    @Test
    void testConvertToDto_NullTimestamp() {
        // Given
        AuditLogPojo pojo = new AuditLogPojo();
        pojo.setId("log456");
        pojo.setOperatorEmail("test@example.com");
        pojo.setOperatorName("Test User");
        pojo.setAction("TEST_ACTION");
        pojo.setTimestamp(null);

        // When
        AuditLogData data = AuditLogHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertNull(data.getTimestamp());
    }
}
