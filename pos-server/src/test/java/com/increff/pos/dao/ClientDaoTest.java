package com.increff.pos.dao;

import com.increff.pos.db.ClientPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ClientDaoTest extends AbstractUnitTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    void testSaveAndFindByClientId() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL001");
        client.setName("test client");
        client.setEmail("test@example.com");
        client.setPhone("1234567890");

        // When
        clientDao.save(client);
        ClientPojo found = clientDao.findByClientId("CL001");

        // Then
        assertNotNull(found);
        assertEquals("CL001", found.getClientId());
        assertEquals("test client", found.getName());
    }

    @Test
    void testFindByName() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL002");
        client.setName("unique name");
        client.setEmail("unique@example.com");
        client.setPhone("9876543210");
        clientDao.save(client);

        // When
        ClientPojo found = clientDao.findByName("unique name");

        // Then
        assertNotNull(found);
        assertEquals("unique name", found.getName());
    }

    @Test
    void testFindByEmail() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL003");
        client.setName("email test");
        client.setEmail("email@test.com");
        client.setPhone("1111111111");
        clientDao.save(client);

        // When
        ClientPojo found = clientDao.findByEmail("email@test.com");

        // Then
        assertNotNull(found);
        assertEquals("email@test.com", found.getEmail());
    }

    @Test
    void testFindByPhone() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL004");
        client.setName("phone test");
        client.setEmail("phone@test.com");
        client.setPhone("5555555555");
        clientDao.save(client);

        // When
        ClientPojo found = clientDao.findByPhone("5555555555");

        // Then
        assertNotNull(found);
        assertEquals("5555555555", found.getPhone());
    }

    @Test
    void testFindByClientId_NotFound() {
        // When
        ClientPojo found = clientDao.findByClientId("NONEXISTENT");

        // Then
        assertNull(found);
    }

    @Test
    void testFindByName_NotFound() {
        // When
        ClientPojo found = clientDao.findByName("nonexistent name");

        // Then
        assertNull(found);
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        ClientPojo found = clientDao.findByEmail("nonexistent@email.com");

        // Then
        assertNull(found);
    }

    @Test
    void testFindByPhone_NotFound() {
        // When
        ClientPojo found = clientDao.findByPhone("0000000000");

        // Then
        assertNull(found);
    }

    @Test
    void testUpdate() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_UPDATE");
        client.setName("original");
        client.setEmail("original@test.com");
        client.setPhone("1234567890");
        clientDao.save(client);

        // When
        client.setName("updated");
        client.setEmail("updated@test.com");
        clientDao.save(client);
        ClientPojo updated = clientDao.findByClientId("CL_UPDATE");

        // Then
        assertNotNull(updated);
        assertEquals("updated", updated.getName());
        assertEquals("updated@test.com", updated.getEmail());
    }

    @Test
    void testDelete() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_DELETE");
        client.setName("to delete");
        client.setEmail("delete@test.com");
        client.setPhone("9999999999");
        ClientPojo saved = clientDao.save(client);

        // When
        clientDao.deleteById(saved.getId());
        ClientPojo found = clientDao.findByClientId("CL_DELETE");

        // Then
        assertNull(found);
    }

    @Test
    void testFindById() {
        // Given
        ClientPojo client = new ClientPojo();
        client.setClientId("CL_FINDID");
        client.setName("find by id");
        client.setEmail("findid@test.com");
        client.setPhone("8888888888");
        ClientPojo saved = clientDao.save(client);

        // When
        Optional<ClientPojo> found = clientDao.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("CL_FINDID", found.get().getClientId());
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<ClientPojo> found = clientDao.findById("nonexistent_id");

        // Then
        assertFalse(found.isPresent());
    }
}
