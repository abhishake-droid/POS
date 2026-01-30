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
    public void testSupervisorLogin() throws ApiException {
        // Create supervisor
        UserForm userForm = new UserForm();
        userForm.setEmail(supervisorEmail);
        userForm.setName("Supervisor");
        userForm.setPassword("superpass");
        try {
            userDto.create(userForm);
        } catch (ApiException e) {
            if (!e.getMessage().contains("already exists")) {
                throw e;
            }
        }

        // Login
        LoginForm loginForm = new LoginForm();
        loginForm.setEmail(supervisorEmail);
        loginForm.setPassword(supervisorPassword);
        AuthData authData = authDto.login(loginForm);

        assertNotNull(authData.getToken());
        assertEquals("SUPERVISOR", authData.getRole());

        // Validate
        AuthData validated = authDto.validateToken(authData.getToken());
        assertEquals("SUPERVISOR", validated.getRole());
    }
}
