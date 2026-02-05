package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidationTest {

    @Test
    void testValidEmail() {
        // Given - Client form with valid email
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.com");

        // When/Then - Should pass validation
        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }

    @Test
    void testEmailTooLong() {
        // Given - Client form with email exceeding 254 characters
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("a".repeat(250) + "@example.com");

        // When/Then - Should throw exception for email too long
        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("Email address is too long"));
    }

    @Test
    void testEmailConsecutiveDots() {
        // Given - Client form with consecutive dots in email
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test..user@example.com");

        // When/Then - Should throw exception for consecutive dots
        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("consecutive dots"));
    }

    @Test
    void testEmailInvalidTLD_OnlyOneChar() {
        // Given - Client form with TLD that's too short (only 1 character)
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.c");

        // When/Then - Should throw exception for invalid TLD
        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void testEmailInvalidTLD_TooLong() {
        // Given - Client form with TLD that's too long (7 characters)
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.abcdefg");

        // When/Then - Should throw exception for invalid TLD
        ApiException exception = assertThrows(ApiException.class,
                () -> ValidationUtil.validate(form));
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void testEmailValidTLD_TwoChars() {
        // Given - Client form with valid 2-character TLD
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.co");

        // When/Then - Should pass validation
        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }

    @Test
    void testEmailValidTLD_SixChars() {
        // Given - Client form with valid 6-character TLD
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("test@example.museum");

        // When/Then - Should pass validation
        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }

    @Test
    void testEmailValidWithSubdomain() {
        // Given - Client form with email containing subdomain
        ClientForm form = new ClientForm();
        form.setName("Test Client");
        form.setPhone("1234567890");
        form.setEmail("user@mail.example.com");

        // When/Then - Should pass validation
        assertDoesNotThrow(() -> ValidationUtil.validate(form));
    }
}
