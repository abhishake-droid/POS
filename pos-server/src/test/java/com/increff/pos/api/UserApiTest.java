package com.increff.pos.api;

import com.increff.pos.db.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserApiTest extends AbstractUnitTest {

    @Autowired
    private UserApi userApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        UserPojo user = new UserPojo();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setName("Test User");
        user.setRole("OPERATOR");

        // When
        UserPojo result = userApi.add(user);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        UserPojo user = new UserPojo();
        user.setEmail("check@example.com");
        user.setPassword("password123");
        user.setName("Check User");
        user.setRole("OPERATOR");
        UserPojo saved = userApi.add(user);

        // When
        UserPojo result = userApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> userApi.getCheck("nonexistent"));
    }

    @Test
    void testGetByEmail_Success() throws ApiException {
        // Given
        UserPojo user = new UserPojo();
        user.setEmail("email@example.com");
        user.setPassword("password123");
        user.setName("Email User");
        user.setRole("OPERATOR");
        userApi.add(user);

        // When
        UserPojo result = userApi.getByEmail("email@example.com");

        // Then
        assertNotNull(result);
        assertEquals("email@example.com", result.getEmail());
    }
}
