package com.increff.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
    private String id;
    private String name;
    private String email;
    private String role; // "SUPERVISOR" or "USER"
} 