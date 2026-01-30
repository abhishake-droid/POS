package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.*;
import org.springframework.util.StringUtils;

public class ValidationUtil {

    public static void validateUserForm(UserForm form) throws ApiException {
        validateEmail(form.getEmail());
        validateName(form.getName());
        validatePassword(form.getPassword());
    }

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

    public static void validateEmail(String email) throws ApiException {
        if (!StringUtils.hasText(email)) {
            throw new ApiException("Email cannot be empty");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ApiException("Invalid email format");
        }
    }

    public static void validateName(String name) throws ApiException {
        if (!StringUtils.hasText(name)) {
            throw new ApiException("Name cannot be empty");
        }
    }

    public static void validatePassword(String password) throws ApiException {
        if (!StringUtils.hasText(password)) {
            throw new ApiException("Password cannot be empty");
        }
        if (password.length() < 6) {
            throw new ApiException("Password must be at least 6 characters long");
        }
    }

    public static void validateLoginForm(LoginForm form) throws ApiException {
        validateEmail(form.getEmail());
        validatePassword(form.getPassword());
    }

    public static void validateOrderForm(OrderForm form) throws ApiException {
        if (form.getLines() == null || form.getLines().isEmpty()) {
            throw new ApiException("Barcode items cannot be empty");
        }
    }

    public static void validateProductForm(ProductForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().trim().isEmpty())
            throw new ApiException("Barcode cannot be empty");
        if (form.getClientId() == null || form.getClientId().trim().isEmpty())
            throw new ApiException("Client ID cannot be empty");
        if (form.getName() == null || form.getName().trim().isEmpty())
            throw new ApiException("Product name cannot be empty");
        if (form.getMrp() == null || form.getMrp() < 0)
            throw new ApiException("MRP must be non-negative");
    }

    public static void validateInventoryForm(InventoryForm form) throws ApiException {
        if (form == null || form.getQuantity() == null || form.getQuantity() < 0) {
            throw new ApiException("Quantity must be non-negative");
        }
    }

    public static void validateClientForm(ClientForm form) throws ApiException {
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new ApiException("Client name cannot be empty");
        }
        if (form.getName().trim().length() < 3 || form.getName().trim().length() > 21) {
            throw new ApiException("Name must be between 3 and 21 characters");
        }
        if (form.getPhone() == null || !form.getPhone().trim().matches("\\d{10}")) {
            throw new ApiException("Phone number must be 10 digits");
        }
        if (form.getEmail() == null
                || !form.getEmail().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ApiException("Invalid email address");
        }
    }
}