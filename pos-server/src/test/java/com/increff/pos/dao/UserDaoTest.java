package com.increff.pos.dao;

import com.increff.pos.db.UserPojo;
import com.increff.pos.test.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest extends AbstractUnitTest {

    @Autowired
    private UserDao userDao;

    @Test
    void testSaveAndFindByEmail() {
        // Given
        UserPojo user = new UserPojo();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole("USER");

        // When
        userDao.save(user);
        UserPojo found = userDao.findByEmail("test@example.com");

        // Then
        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());
        assertEquals("Test User", found.getName());
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        UserPojo found = userDao.findByEmail("nonexistent@example.com");

        // Then
        assertNull(found);
    }

    @Test
    void testSaveWithRole() {
        // Given
        UserPojo user = new UserPojo();
        user.setName("Admin User");
        user.setEmail("admin@example.com");
        user.setPassword("admin123");
        user.setRole("ADMIN");

        // When
        UserPojo saved = userDao.save(user);

        // Then
        assertNotNull(saved.getId());
        assertEquals("ADMIN", saved.getRole());
    }
}
