package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TsvUtilTest {

    @Test
    void testEncode() {
        // Given - TSV content with tabs and newlines
        String content = "Hello\tWorld\nLine2\tData";

        // When - Encode to base64
        String encoded = TsvUtil.encode(content);

        // Then - Should be encoded without tabs or newlines
        assertNotNull(encoded);
        assertFalse(encoded.contains("\t"));
        assertFalse(encoded.contains("\n"));
    }

    @Test
    void testEncode_EmptyString() {
        // Given - Empty string
        // When - Encode it
        String encoded = TsvUtil.encode("");

        // Then - Should return non-null encoded string
        assertNotNull(encoded);
    }

    @Test
    void testDecode_Valid() throws ApiException {
        // Given - Valid TSV content
        String original = "Product1\t100\t50.00\nProduct2\t200\t75.50";
        String encoded = TsvUtil.encode(original);

        // When - Decode it back
        String decoded = TsvUtil.decode(encoded);

        // Then - Should match original content
        assertEquals(original, decoded);
    }

    @Test
    void testDecode_InvalidBase64_ThrowsException() {
        // Given - Invalid base64 string
        String invalidBase64 = "This is not valid base64!@#$";

        // When/Then - Should throw exception
        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.decode(invalidBase64));
        assertEquals("Invalid base64 encoding", exception.getMessage());
    }

    @Test
    void testSplitLines_MultipleLines() throws ApiException {
        // Given - Content with multiple lines
        String content = "Line1\nLine2\nLine3";

        // When - Split into lines
        String[] lines = TsvUtil.splitLines(content);

        // Then - Should have 3 lines
        assertEquals(3, lines.length);
        assertEquals("Line1", lines[0]);
        assertEquals("Line2", lines[1]);
        assertEquals("Line3", lines[2]);
    }

    @Test
    void testSplitLines_SingleLine() throws ApiException {
        // Given - Content with single line
        String content = "SingleLine";

        // When - Split into lines
        String[] lines = TsvUtil.splitLines(content);

        // Then - Should have 1 line
        assertEquals(1, lines.length);
        assertEquals("SingleLine", lines[0]);
    }

    @Test
    void testSplitLines_Null_ThrowsException() {
        // Given - Null content
        // When/Then - Should throw exception
        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.splitLines(null));
        assertEquals("Content is empty", exception.getMessage());
    }

    @Test
    void testSplitLines_Empty_ThrowsException() {
        // Given - Empty content
        // When/Then - Should throw exception
        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.splitLines(""));
        assertEquals("Content is empty", exception.getMessage());
    }

    @Test
    void testSplitLines_TooManyRows_ThrowsException() {
        // Given - Content with more than 5000 lines
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5001; i++) {
            sb.append("Line").append(i).append("\n");
        }

        // When/Then - Should throw exception for exceeding max rows
        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.splitLines(sb.toString()));
        assertEquals("Maximum 5000 rows allowed", exception.getMessage());
    }

    @Test
    void testEncodeDecodeRoundTrip() throws ApiException {
        // Given - TSV content with multiple rows and columns
        String original = "Barcode\tName\tPrice\nBC001\tProduct A\t100.50\nBC002\tProduct B\t200.75";

        // When - Encode and then decode
        String encoded = TsvUtil.encode(original);
        String decoded = TsvUtil.decode(encoded);

        // Then - Should match original exactly
        assertEquals(original, decoded);
    }

    @Test
    void testEncodeDecodeRoundTrip_SpecialCharacters() throws ApiException {
        // Given - Content with special characters and unicode
        String original = "Special\tChars: !@#$%^&*()\nUnicode: ñ é ü";

        // When - Encode and decode
        String encoded = TsvUtil.encode(original);
        String decoded = TsvUtil.decode(encoded);

        // Then - Should preserve special characters
        assertEquals(original, decoded);
    }

    @Test
    void testSplitLines_WindowsLineEndings() throws ApiException {
        // Given - Content with Windows-style line endings (\r\n)
        String content = "Line1\r\nLine2\r\nLine3";

        // When - Split into lines
        String[] lines = TsvUtil.splitLines(content);

        // Then - Should handle Windows line endings correctly
        assertEquals(3, lines.length);
        assertEquals("Line1", lines[0]);
        assertEquals("Line2", lines[1]);
        assertEquals("Line3", lines[2]);
    }
}
