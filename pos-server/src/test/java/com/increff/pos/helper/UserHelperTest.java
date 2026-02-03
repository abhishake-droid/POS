package com.increff.pos.helper;

import com.increff.pos.db.UserPojo;
import com.increff.pos.model.data.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserHelperTest {

    @Test
    void testConvertToEntity() {
        // Given
        com.increff.pos.model.form.UserForm form = new com.increff.pos.model.form.UserForm();
        form.setName("John Doe");
        form.setEmail("john@example.com");
        form.setRole("ADMIN");
        form.setPassword("password123");

        // When
        UserPojo pojo = UserHelper.convertToEntity(form);

        // Then
        assertNotNull(pojo);
        assertEquals("John Doe", pojo.getName());
        assertEquals("john@example.com", pojo.getEmail());
        assertEquals("ADMIN", pojo.getRole());
        assertEquals("password123", pojo.getPassword());
    }

    @Test
    void testConvertToEntity_NullRole_DefaultsToUser() {
        // Given
        com.increff.pos.model.form.UserForm form = new com.increff.pos.model.form.UserForm();
        form.setName("Jane Doe");
        form.setEmail("jane@example.com");
        form.setRole(null);
        form.setPassword("pass456");

        // When
        UserPojo pojo = UserHelper.convertToEntity(form);

        // Then
        assertNotNull(pojo);
        assertEquals("USER", pojo.getRole());
    }

    @Test
    void testConvertToDto() {
        // Given
        UserPojo pojo = new UserPojo();
        pojo.setId("user123");
        pojo.setName("Alice Smith");
        pojo.setEmail("alice@example.com");
        pojo.setRole("SUPERVISOR");

        // When
        UserData data = UserHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertEquals("user123", data.getId());
        assertEquals("Alice Smith", data.getName());
        assertEquals("alice@example.com", data.getEmail());
        assertEquals("SUPERVISOR", data.getRole());
    }

    @Test
    void testConvertToDto_NullRole_DefaultsToUser() {
        // Given
        UserPojo pojo = new UserPojo();
        pojo.setId("user456");
        pojo.setName("Bob Johnson");
        pojo.setEmail("bob@example.com");
        pojo.setRole(null);

        // When
        UserData data = UserHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertEquals("USER", data.getRole());
    }

    @Test
    void testConvertToDto_NullId() {
        // Given
        UserPojo pojo = new UserPojo();
        pojo.setId(null);
        pojo.setName("Test User");
        pojo.setEmail("test@example.com");
        pojo.setRole("USER");

        // When
        UserData data = UserHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertNull(data.getId());
        assertEquals("Test User", data.getName());
    }
}
