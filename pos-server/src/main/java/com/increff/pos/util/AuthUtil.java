package com.increff.pos.util;

import com.increff.pos.dto.AuthDto;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.AuthData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthUtil {
    private final AuthDto authDto;

    public AuthUtil(AuthDto authDto) {
        this.authDto = authDto;
    }

    public AuthData getAuthFromRequest(HttpServletRequest request) throws ApiException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            throw new ApiException("Authentication required");
        }

        String token = authHeader;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token.isEmpty()) {
            throw new ApiException("Authentication required");
        }
        
        return authDto.validateToken(token);
    }

    public boolean isSupervisor(HttpServletRequest request) {
        try {
            AuthData auth = getAuthFromRequest(request);
            return "SUPERVISOR".equals(auth.getRole());
        } catch (Exception e) {
            return false;
        }
    }
}
