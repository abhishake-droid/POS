package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidationTest {

    @Test
    void testValidEmail() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.com");

        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }

    @Test
    void testEmailTooLong() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        // Create email > 254 characters
        form.setEmail("a".repeat(250) + "@example.com");

        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("Email address is too long"));
    }

    @Test
    void testEmailConsecutiveDots() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test..user@example.com");

        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("consecutive dots"));
    }

    @Test
    void testEmailInvalidTLD_OnlyOneChar() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.c"); // TLD too short

        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void testEmailInvalidTLD_TooLong() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.abcdefg"); // TLD too long (7 chars)

        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void testEmailValidTLD_TwoChars() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.co");

        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }

    @Test
    void testEmailValidTLD_SixChars() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.museum"); // 6 chars - valid

        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }

    @Test
    void testEmailValidWithSubdomain() {
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("user@mail.example.com");

        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }
}
