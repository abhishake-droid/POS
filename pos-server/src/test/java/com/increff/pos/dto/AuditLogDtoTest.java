package com.increff.pos.dto;

import com.increff.pos.test.AbstractUnitTest;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuditLogData;
import com.increff.pos.model.form.PageForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuditLogDtoTest extends AbstractUnitTest {

    @Autowired
    private AuditLogDto auditLogDto;

    @Test
    public void testGetByOperatorEmail_EmptyResult() {
        // When
        List<AuditLogData> result = auditLogDto.getByOperatorEmail("nonexistent@example.com");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAll_WithPagination_ValidParams() throws ApiException {
        // Given
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(10);

        // When
        Page<AuditLogData> result = auditLogDto.getAll(form);

        // Then
        assertNotNull(result);
        assertNotNull(result.getContent());
    }

    @Test
    public void testGetAll_WithPagination_InvalidPageNumber() {
        // Given
        PageForm form = new PageForm();
        form.setPage(-1);
        form.setSize(10);

        // When/Then
        assertThrows(ApiException.class, () -> auditLogDto.getAll(form));
    }

    @Test
    public void testGetAll_WithPagination_InvalidPageSize() {
        // Given
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(0);

        // When/Then
        assertThrows(ApiException.class, () -> auditLogDto.getAll(form));
    }

    @Test
    public void testGetAll_WithPagination_PageSizeTooLarge() {
        // Given
        PageForm form = new PageForm();
        form.setPage(0);
        form.setSize(101); // Max is 100

        // When/Then
        assertThrows(ApiException.class, () -> auditLogDto.getAll(form));
    }

    @Test
    public void testGetAll_WithoutPagination() {
        // When
        List<AuditLogData> result = auditLogDto.getAll();

        // Then
        assertNotNull(result);
    }

    @Test
    public void testGetAll_WithPagination_DifferentPageSizes() throws ApiException {
        // Test with page size 5
        PageForm form1 = new PageForm();
        form1.setPage(0);
        form1.setSize(5);
        Page<AuditLogData> result1 = auditLogDto.getAll(form1);
        assertNotNull(result1);

        // Test with page size 20
        PageForm form2 = new PageForm();
        form2.setPage(0);
        form2.setSize(20);
        Page<AuditLogData> result2 = auditLogDto.getAll(form2);
        assertNotNull(result2);
    }

    @Test
    public void testGetAll_WithPagination_SecondPage() throws ApiException {
        // Given
        PageForm form = new PageForm();
        form.setPage(1);
        form.setSize(10);

        // When
        Page<AuditLogData> result = auditLogDto.getAll(form);

        // Then
        assertNotNull(result);
        assertNotNull(result.getContent());
    }

    @Test
    public void testGetByOperatorEmail_WithNullEmail() {
        // When
        List<AuditLogData> result = auditLogDto.getByOperatorEmail(null);

        // Then
        assertNotNull(result);
    }

    @Test
    public void testGetByOperatorEmail_WithEmptyEmail() {
        // When
        List<AuditLogData> result = auditLogDto.getByOperatorEmail("");

        // Then
        assertNotNull(result);
    }
}
