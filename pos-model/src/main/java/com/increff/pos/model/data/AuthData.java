package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthData {
    private String token;
    private String email;
    private String name;
    private String role;
}
