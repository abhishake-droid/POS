package com.increff.pos.helper;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.model.data.ClientData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientHelperTest {

    @Test
    void testConvertToEntity() {
        // Given
        com.increff.pos.model.form.ClientForm form = new com.increff.pos.model.form.ClientForm();
        form.setName("John Doe");
        form.setEmail("john@example.com");
        form.setPhone("1234567890");

        // When
        ClientPojo pojo = ClientHelper.convertToEntity(form);

        // Then
        assertNotNull(pojo);
        assertEquals("John Doe", pojo.getName());
        assertEquals("john@example.com", pojo.getEmail());
        assertEquals("1234567890", pojo.getPhone());
    }

    @Test
    void testConvertToDto() {
        // Given
        ClientPojo pojo = new ClientPojo();
        pojo.setId("client123");
        pojo.setClientId("CL001");
        pojo.setName("Jane Doe");
        pojo.setEmail("jane@example.com");
        pojo.setPhone("0987654321");

        // When
        ClientData data = ClientHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertEquals("client123", data.getId());
        assertEquals("CL001", data.getClientId());
        assertEquals("Jane Doe", data.getName());
        assertEquals("jane@example.com", data.getEmail());
        assertEquals("0987654321", data.getPhone());
    }

    @Test
    void testConvertToEntity_NullValues() {
        // Given
        com.increff.pos.model.form.ClientForm form = new com.increff.pos.model.form.ClientForm();
        form.setName(null);
        form.setEmail(null);
        form.setPhone(null);

        // When
        ClientPojo pojo = ClientHelper.convertToEntity(form);

        // Then
        assertNotNull(pojo);
        assertNull(pojo.getName());
        assertNull(pojo.getEmail());
        assertNull(pojo.getPhone());
    }

    @Test
    void testConvertToDto_NullId() {
        // Given
        ClientPojo pojo = new ClientPojo();
        pojo.setId(null);
        pojo.setClientId(null);
        pojo.setName("Test");
        pojo.setEmail("test@example.com");
        pojo.setPhone("1111111111");

        // When
        ClientData data = ClientHelper.convertToData(pojo);

        // Then
        assertNotNull(data);
        assertNull(data.getId());
        assertNull(data.getClientId());
        assertEquals("Test", data.getName());
    }
}
