package com.increff.pos.util;

import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.UserForm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NormalizeUtilTest {

    @Test
    void testNormalizeClientForm() {
        // Given - Client form with mixed case and whitespace
        ClientForm form = new ClientForm();
        form.setName("  John Doe  ");
        form.setEmail("  JOHN@EXAMPLE.COM  ");
        form.setPhone("  1234567890  ");

        // When - Normalize the form
        NormalizeUtil.normalizeClientForm(form);

        // Then - All fields should be trimmed and lowercased
        assertEquals("john doe", form.getName());
        assertEquals("john@example.com", form.getEmail());
        assertEquals("1234567890", form.getPhone());
    }

    @Test
    void testNormalizeClientForm_AlreadyNormalized() {
        // Given - Client form already normalized
        ClientForm form = new ClientForm();
        form.setName("john doe");
        form.setEmail("john@example.com");
        form.setPhone("1234567890");

        // When - Normalize again
        NormalizeUtil.normalizeClientForm(form);

        // Then - Should remain unchanged
        assertEquals("john doe", form.getName());
        assertEquals("john@example.com", form.getEmail());
        assertEquals("1234567890", form.getPhone());
    }

    @Test
    void testNormalizeProductForm() {
        // Given - Product form with mixed case and whitespace
        ProductForm form = new ProductForm();
        form.setBarcode("  BC123  ");
        form.setName("  Product Name  ");
        form.setClientId("  client123  ");
        form.setImageUrl("  http://example.com/image.jpg  ");

        // When - Normalize the form
        NormalizeUtil.normalizeProductForm(form);

        // Then - Barcode and name should be lowercased and trimmed
        assertEquals("bc123", form.getBarcode());
        assertEquals("product name", form.getName());
        assertEquals("client123", form.getClientId());
        assertEquals("http://example.com/image.jpg", form.getImageUrl());
    }

    @Test
    void testNormalizeProductForm_NullImageUrl() {
        // Given - Product form with null image URL
        ProductForm form = new ProductForm();
        form.setBarcode("BC123");
        form.setName("Product");
        form.setClientId("client123");
        form.setImageUrl(null);

        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> NormalizeUtil.normalizeProductForm(form));
        assertNull(form.getImageUrl());
    }

    @Test
    void testNormalizeUserForm() {
        // Given - User form with mixed case and whitespace
        UserForm form = new UserForm();
        form.setName("  Jane Doe  ");
        form.setEmail("  JANE@EXAMPLE.COM  ");

        // When - Normalize the form
        NormalizeUtil.normalizeUserForm(form);

        // Then - Name should be trimmed (not lowercased), email lowercased
        assertEquals("Jane Doe", form.getName());
        assertEquals("jane@example.com", form.getEmail());
    }

    @Test
    void testNormalizeUserForm_NullValues() {
        // Given - User form with null values
        UserForm form = new UserForm();
        form.setName(null);
        form.setEmail(null);

        // When/Then - Should handle nulls gracefully
        assertDoesNotThrow(() -> NormalizeUtil.normalizeUserForm(form));

        assertNull(form.getName());
        assertNull(form.getEmail());
    }

    @Test
    void testNormalizeUserForm_OnlyNameNull() {
        // Given - User form with only name null
        UserForm form = new UserForm();
        form.setName(null);
        form.setEmail("  TEST@EXAMPLE.COM  ");

        // When - Normalize the form
        NormalizeUtil.normalizeUserForm(form);

        // Then - Email should be normalized, name remains null
        assertNull(form.getName());
        assertEquals("test@example.com", form.getEmail());
    }

    @Test
    void testNormalizeProductForm_MixedCase() {
        // Given - Product form with mixed case values
        ProductForm form = new ProductForm();
        form.setBarcode("AbC123DeF");
        form.setName("MiXeD CaSe PrOdUcT");
        form.setClientId("ClientID");

        // When - Normalize the form
        NormalizeUtil.normalizeProductForm(form);

        // Then - Barcode and name lowercased, clientId unchanged
        assertEquals("abc123def", form.getBarcode());
        assertEquals("mixed case product", form.getName());
        assertEquals("ClientID", form.getClientId());
    }
}
