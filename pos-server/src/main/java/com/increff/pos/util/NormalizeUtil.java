package com.increff.pos.util;

import com.increff.pos.model.form.*;

public class NormalizeUtil {

    public static void normalizeClientForm(ClientForm form) {
        form.setName(form.getName().trim().toLowerCase());
        form.setPhone(form.getPhone().trim());
        form.setEmail(form.getEmail().trim().toLowerCase());
    }

    public static void normalizeProductForm(ProductForm form) {
        form.setBarcode(form.getBarcode().trim().toLowerCase());
        form.setClientId(form.getClientId().trim());
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
}
