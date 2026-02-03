package com.increff.pos.dto;

import com.increff.pos.test.AbstractUnitTest;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuthData;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.UserForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class AuthDtoTest extends AbstractUnitTest {

    @Autowired
    private AuthDto authDto;

    @Autowired
    private UserDto userDto;

    @Value("${supervisor.email}")
    private String supervisorEmail;

    @Value("${supervisor.password}")
    private String supervisorPassword;

    @Test
    public void testLoginAndValidate() throws ApiException {
        // Create a user first
        UserForm userForm = new UserForm();
        userForm.setEmail("test@example.com");
        userForm.setName("Test User");
        userForm.setPassword("testpass123");
        userDto.create(userForm);

        // Login
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("test@example.com");
        loginForm.setPassword("testpass123");
        AuthData authData = authDto.login(loginForm);

        assertNotNull(authData.getToken());
        assertEquals("test@example.com", authData.getEmail());

        // Validate token
        AuthData validatedData = authDto.validateToken(authData.getToken());
        assertNotNull(validatedData);
        assertEquals(authData.getEmail(), validatedData.getEmail());
    }

    @Test
    public void testInvalidTokenWithWrongServerId() throws ApiException {
        // Create user
        UserForm userForm = new UserForm();
        userForm.setEmail("invalid@example.com");
        userForm.setName("Invalid User");
        userForm.setPassword("pass123");
        userDto.create(userForm);

        // Construct a token with a different server ID manually
        String wrongServerId = "wrong-server-id";
        String tokenPayload = "invalid@example.com:USER:" + wrongServerId;
        String invalidToken = Base64.getEncoder().encodeToString(tokenPayload.getBytes());

        // Validating this token should fail
        ApiException exception = assertThrows(ApiException.class, () -> authDto.validateToken(invalidToken));
        assertEquals("Session expired or server restarted. Please login again.", exception.getMessage());
    }

    @Test
    public void testLoginWithInvalidPassword() throws ApiException {
        // Create user
        UserForm userForm = new UserForm();
        userForm.setEmail("wrongpass@example.com");
        userForm.setName("Wrong Pass User");
        userForm.setPassword("correctpass");
        userDto.create(userForm);

        // Try to login with wrong password
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("wrongpass@example.com");
        loginForm.setPassword("wrongpassword");

        ApiException exception = assertThrows(ApiException.class, () -> authDto.login(loginForm));
        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    @Test
    public void testLoginWithNonExistentUser() {
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("nonexistent@example.com");
        loginForm.setPassword("anypassword");

        ApiException exception = assertThrows(ApiException.class, () -> authDto.login(loginForm));
        assertTrue(exception.getMessage().contains("not found") || exception.getMessage().contains("Invalid"));
    }

    @Test
    public void testValidateTokenWithMalformedToken() {
        String malformedToken = "not-a-valid-base64-token-format";

        ApiException exception = assertThrows(ApiException.class, () -> authDto.validateToken(malformedToken));
        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    public void testValidateTokenWithInvalidFormat() {
        // Token with only 2 parts instead of 3
        String tokenPayload = "email@example.com:USER"; // Missing server ID
        String invalidToken = Base64.getEncoder().encodeToString(tokenPayload.getBytes());

        ApiException exception = assertThrows(ApiException.class, () -> authDto.validateToken(invalidToken));
        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    public void testValidateTokenForDeletedUser() throws ApiException {
        // Create user and get token
        UserForm userForm = new UserForm();
        userForm.setEmail("tobedeleted@example.com");
        userForm.setName("To Be Deleted");
        userForm.setPassword("password123");
        userDto.create(userForm);

        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("tobedeleted@example.com");
        loginForm.setPassword("password123");
        AuthData authData = authDto.login(loginForm);

        // Note: We can't actually delete the user in this test setup,
        // but we can test with a token for a non-existent user
        String nonExistentUserToken = Base64.getEncoder().encodeToString(
                ("nonexistent@example.com:USER:" + extractServerIdFromToken(authData.getToken())).getBytes());

        ApiException exception = assertThrows(ApiException.class, () -> authDto.validateToken(nonExistentUserToken));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testLoginWithEmptyEmail() {
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("");
        loginForm.setPassword("password");

        ApiException exception = assertThrows(ApiException.class, () -> authDto.login(loginForm));
        assertTrue(exception.getMessage().contains("Email"));
    }

    @Test
    public void testLoginWithEmptyPassword() {
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail("test@example.com");
        loginForm.setPassword("");

        ApiException exception = assertThrows(ApiException.class, () -> authDto.login(loginForm));
        assertTrue(exception.getMessage().contains("Password"));
    }

    // Helper method to extract server ID from token
    private String extractServerIdFromToken(String token) {
        String decoded = new String(Base64.getDecoder().decode(token));
        String[] parts = decoded.split(":");
        return parts.length >= 3 ? parts[2] : "";
    }
}
