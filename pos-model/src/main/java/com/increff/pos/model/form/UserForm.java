package com.increff.pos.model.form;

import lombok.Data;

@Data
public class UserForm {
    private String name;
    private String email;
    private String password; // Required for supervisor, optional for user
    private String role; // "SUPERVISOR" or "USER"
} 