package com.increff.pos.controller;

import com.increff.pos.dto.AuthDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuthData;
import com.increff.pos.model.form.LoginForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "APIs for user authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthDto authDto;

    public AuthController(AuthDto authDto) {
        this.authDto = authDto;
    }

    @Operation(summary = "Login user")
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public AuthData login(@RequestBody LoginForm loginForm) throws ApiException {
        return authDto.login(loginForm);
    }

    @Operation(summary = "Validate token")
    @RequestMapping(path = "/validate", method = RequestMethod.POST)
    public AuthData validateToken(@RequestBody java.util.Map<String, String> request) throws ApiException {
        String token = request.get("token");
        if (token == null || token.trim().isEmpty()) {
            throw new ApiException("Token is required");
        }
        return authDto.validateToken(token);
    }

    @Operation(summary = "Logout user")
    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(@RequestBody java.util.Map<String, String> request) throws ApiException {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException("Email is required");
        }
        authDto.logLogout(email);
    }
}
