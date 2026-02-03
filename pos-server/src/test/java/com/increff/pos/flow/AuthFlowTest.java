package com.increff.pos.flow;

import com.increff.pos.api.UserApi;
import com.increff.pos.api.AuditLogApi;
import com.increff.pos.db.UserPojo;
import com.increff.pos.db.AuditLogPojo;
import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthFlowTest {

    @Mock
    private UserApi userApi;

    @Mock
    private AuditLogApi auditLogApi;

    @InjectMocks
    private AuthFlow authFlow;

    private UserPojo userPojo;

    @BeforeEach
    void setUp() {
        userPojo = new UserPojo();
        userPojo.setId("user1");
        userPojo.setEmail("test@example.com");
        userPojo.setName("Test User");
        userPojo.setRole("OPERATOR");
    }

    @Test
    void testGetUserAndLogActivity_Success() throws ApiException {
        // Given
        when(userApi.getByEmail("test@example.com")).thenReturn(userPojo);
        when(auditLogApi.add(any(AuditLogPojo.class))).thenReturn(new AuditLogPojo());

        // When
        UserPojo result = authFlow.getUserAndLogActivity("test@example.com", "Test User", "LOGIN");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userApi, times(1)).getByEmail("test@example.com");
        verify(auditLogApi, times(1)).add(any(AuditLogPojo.class));
    }

    @Test
    void testGetUserByEmail_Success() throws ApiException {
        // Given
        when(userApi.getByEmail("test@example.com")).thenReturn(userPojo);

        // When
        UserPojo result = authFlow.getUserByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userApi, times(1)).getByEmail("test@example.com");
    }

    @Test
    void testLogActivity_Success() throws ApiException {
        // Given
        when(auditLogApi.add(any(AuditLogPojo.class))).thenReturn(new AuditLogPojo());

        // When
        authFlow.logActivity("test@example.com", "Test User", "LOGOUT");

        // Then
        verify(auditLogApi, times(1)).add(argThat(log -> log.getOperatorEmail().equals("test@example.com") &&
                log.getAction().equals("LOGOUT")));
    }

    @Test
    void testLogActivity_HandlesException() throws ApiException {
        // Given
        when(auditLogApi.add(any(AuditLogPojo.class))).thenThrow(new ApiException("Database error"));

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> authFlow.logActivity("test@example.com", "Test User", "ACTION"));
    }
}
