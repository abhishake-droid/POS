package com.increff.pos.model.data;

import lombok.Data;

@Data
public class AuthData {
    private String token;
    private String email;
    private String name;
    private String role; // "SUPERVISOR" or "USER"
}
