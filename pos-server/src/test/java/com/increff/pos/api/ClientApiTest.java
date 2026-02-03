package com.increff.pos.api;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ClientApiTest extends AbstractUnitTest {

    @Autowired
    private ClientApi clientApi;

    @Test
    void testAdd_Success() throws ApiException {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL001"); // Will be overridden by auto-generation
        client.setName("test client");
        client.setEmail("test@example.com");
        client.setPhone("1234567890");

        // When
        ClientPojo result = clientApi.add(client);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getClientId()); // Auto-generated
        assertTrue(result.getClientId().startsWith("C")); // Format: C0001, C0002, etc.
        assertEquals("test client", result.getName());
    }

    @Test
    void testGetCheck_Success() throws ApiException {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_GET"); // Will be auto-generated
        client.setName("get client");
        client.setEmail("get@example.com");
        client.setPhone("3333333333");
        ClientPojo saved = clientApi.add(client);

        // When
        ClientPojo result = clientApi.getCheck(saved.getId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getId(), result.getId());
        assertEquals(saved.getClientId(), result.getClientId()); // Use auto-generated ID
    }

    @Test
    void testGetCheck_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> clientApi.getCheck("nonexistent_id"));
    }

    @Test
    void testGetCheckByClientId_Success() throws ApiException {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_GETBY"); // Will be auto-generated
        client.setName("getby client");
        client.setEmail("getby@example.com");
        client.setPhone("4444444444");
        ClientPojo saved = clientApi.add(client);

        // When - Use the auto-generated clientId
        ClientPojo result = clientApi.getCheckByClientId(saved.getClientId());

        // Then
        assertNotNull(result);
        assertEquals(saved.getClientId(), result.getClientId());
    }

    @Test
    void testUpdate_Success() throws ApiException {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_UPDATE");
        client.setName("original name");
        client.setEmail("original@example.com");
        client.setPhone("5555555555");
        ClientPojo saved = clientApi.add(client);

        // When
        saved.setName("updated name");
        saved.setEmail("updated@example.com");
        saved.setPhone("6666666666");
        ClientPojo updated = clientApi.update(saved.getId(), saved);

        // Then
        assertEquals("updated name", updated.getName());
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("6666666666", updated.getPhone());
    }

    @Test
    void testGetCheckByClientId_NotFound() {
        // When/Then
        assertThrows(ApiException.class, () -> clientApi.getCheckByClientId("NONEXISTENT"));
    }

    @Test
    void testUpdate_NotFound() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setName("Updated");

        // When/Then
        assertThrows(ApiException.class, () -> clientApi.update("nonexistent", client));
    }

    @Test
    void testAdd_DuplicateEmail() throws ApiException {
        // Given
        ClientPojo client1 = new ClientPojo();
        client1.setClientId("CL_DUP1");
        client1.setName("client 1");
        client1.setEmail("duplicate@example.com");
        client1.setPhone("8888888888");
        clientApi.add(client1);

        ClientPojo client2 = new ClientPojo();
        client2.setClientId("CL_DUP2");
        client2.setName("client 2");
        client2.setEmail("duplicate@example.com"); // Same email
        client2.setPhone("9999999999");

        // When/Then - Should throw exception for duplicate email
        assertThrows(ApiException.class, () -> clientApi.add(client2));
    }

    @Test
    void testGetAll_WithPagination() throws ApiException {
        // Given - Add multiple clients
        for (int i = 0; i < 5; i++) {
            ClientPojo client = new ClientPojo();
            client.setClientId("CL_PAGE" + i);
            client.setName("Client " + i);
            client.setEmail("client" + i + "@example.com");
            client.setPhone("100000000" + i);
            clientApi.add(client);
        }

        // When - Get first page
        var page = clientApi.getAll(0, 3);

        // Then
        assertNotNull(page);
        assertTrue(page.getContent().size() <= 3);
    }

    @Test
    void testUpdate_ChangeEmail() throws ApiException {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_EMAIL");
        client.setName("Email Test");
        client.setEmail("old@example.com");
        client.setPhone("7777777777");
        ClientPojo saved = clientApi.add(client);

        // When
        saved.setEmail("new@example.com");
        ClientPojo updated = clientApi.update(saved.getId(), saved);

        // Then
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void testGetAll_EmptyDatabase() throws ApiException {
        // When - Get all from potentially empty or sparse database
        var page = clientApi.getAll(0, 10);

        // Then - Should not throw, just return results
        assertNotNull(page);
    }
}
