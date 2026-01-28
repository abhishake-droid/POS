package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.UserForm;
import com.increff.pos.model.form.PageForm;
import org.springframework.util.StringUtils;

public class ValidationUtil {

    // User validations
    public static void validateUserForm(UserForm form) throws ApiException {
        validateEmail(form.getEmail());
        validateName(form.getName());
        validatePassword(form.getPassword());
    }

    private static void validatePassword(String password) throws ApiException {
        if (!StringUtils.hasText(password)) {
            throw new ApiException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new ApiException("Password must be at least 6 characters long");
        }
    }

    private static void validateEmail(String email) throws ApiException {
        if (!StringUtils.hasText(email)) {
            throw new ApiException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ApiException("Invalid email format");
        }
    }

    private static void validateName(String name) throws ApiException {
        if (!StringUtils.hasText(name)) {
            throw new ApiException("Name cannot be empty");
        }
    }

    // Pagination validations
    public static void validatePageForm(PageForm form) throws ApiException {
        if (form.getPage() < 0) {
            throw new ApiException("Page number cannot be negative");
        }
        if (form.getSize() <= 0) {
            throw new ApiException("Page size must be positive");
        }
        if (form.getSize() > 100) {
            throw new ApiException("Page size cannot be greater than 100");
        }
    }
}