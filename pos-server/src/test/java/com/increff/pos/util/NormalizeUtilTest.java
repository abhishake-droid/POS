package com.increff.pos.util;

import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.UserForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizeUtilTest {

    @Test
    void testNormalizeClientForm() {
        ClientForm form = new ClientForm();
        form.setName("  John Doe  ");
        form.setEmail("  JOHN@EXAMPLE.COM  ");
        form.setPhone("  1234567890  ");

        NormalizeUtil.normalizeClientForm(form);

        assertEquals("john doe", form.getName());
        assertEquals("john@example.com", form.getEmail());
        assertEquals("1234567890", form.getPhone());
    }

    @Test
    void testNormalizeClientForm_AlreadyNormalized() {
        ClientForm form = new ClientForm();
        form.setName("john doe");
        form.setEmail("john@example.com");
        form.setPhone("1234567890");

        NormalizeUtil.normalizeClientForm(form);

        assertEquals("john doe", form.getName());
        assertEquals("john@example.com", form.getEmail());
        assertEquals("1234567890", form.getPhone());
    }

    @Test
    void testNormalizeProductForm() {
        ProductForm form = new ProductForm();
        form.setBarcode("  BC123  ");
        form.setName("  Product Name  ");
        form.setClientId("  client123  ");
        form.setImageUrl("  http://example.com/image.jpg  ");

        NormalizeUtil.normalizeProductForm(form);

        assertEquals("bc123", form.getBarcode());
        assertEquals("product name", form.getName());
        assertEquals("client123", form.getClientId());
        assertEquals("http://example.com/image.jpg", form.getImageUrl());
    }

    @Test
    void testNormalizeProductForm_NullImageUrl() {
        ProductForm form = new ProductForm();
        form.setBarcode("BC123");
        form.setName("Product");
        form.setClientId("client123");
        form.setImageUrl(null);

        assertDoesNotThrow(() -> NormalizeUtil.normalizeProductForm(form));
        assertNull(form.getImageUrl());
    }

    @Test
    void testNormalizeUserForm() {
        UserForm form = new UserForm();
        form.setName("  Jane Doe  ");
        form.setEmail("  JANE@EXAMPLE.COM  ");

        NormalizeUtil.normalizeUserForm(form);

        assertEquals("Jane Doe", form.getName());
        assertEquals("jane@example.com", form.getEmail());
    }

    @Test
    void testNormalizeUserForm_NullValues() {
        UserForm form = new UserForm();
        form.setName(null);
        form.setEmail(null);

        assertDoesNotThrow(() -> NormalizeUtil.normalizeUserForm(form));

        assertNull(form.getName());
        assertNull(form.getEmail());
    }

    @Test
    void testNormalizeUserForm_OnlyNameNull() {
        UserForm form = new UserForm();
        form.setName(null);
        form.setEmail("  TEST@EXAMPLE.COM  ");

        NormalizeUtil.normalizeUserForm(form);

        assertNull(form.getName());
        assertEquals("test@example.com", form.getEmail());
    }

    @Test
    void testNormalizeProductForm_MixedCase() {
        ProductForm form = new ProductForm();
        form.setBarcode("AbC123DeF");
        form.setName("MiXeD CaSe PrOdUcT");
        form.setClientId("ClientID");

        NormalizeUtil.normalizeProductForm(form);

        assertEquals("abc123def", form.getBarcode());
        assertEquals("mixed case product", form.getName());
        assertEquals("ClientID", form.getClientId());
    }
}
