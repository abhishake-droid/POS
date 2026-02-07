package com.increff.pos.util;

import com.increff.pos.model.form.*;

public class NormalizeUtil {

    public static void normalizeClientForm(ClientForm form) {
        if (form.getName() != null)
            form.setName(form.getName().trim().toLowerCase());
        if (form.getPhone() != null)
            form.setPhone(form.getPhone().trim());
        if (form.getEmail() != null)
            form.setEmail(form.getEmail().trim().toLowerCase());
    }

    public static void normalizeProductForm(ProductForm form) {
        if (form.getBarcode() != null)
            form.setBarcode(form.getBarcode().trim().toLowerCase());
        if (form.getClientId() != null)
            form.setClientId(form.getClientId().trim());
        if (form.getName() != null)
            form.setName(form.getName().trim().toLowerCase());
        if (form.getImageUrl() != null)
            form.setImageUrl(form.getImageUrl().trim());
    }

    public static void normalizeUserForm(UserForm form) {
        if (form.getName() != null)
            form.setName(form.getName().trim());
        if (form.getEmail() != null)
            form.setEmail(form.getEmail().trim().toLowerCase());
    }

    public static String normalizeBarcode(String barcode) {
        return barcode.trim().toLowerCase();
    }

    public static String normalizeOrderId(String orderId) {
        return orderId != null ? orderId.trim() : null;
    }

    public static String normalizeId(String id) {
        return id != null ? id.trim() : null;
    }

    public static String normalizeSearchString(String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return null;
        }
        return searchString.trim();
    }
}
