package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TsvUtilTest {

    @Test
    void testEncode() {
        String content = "Hello\tWorld\nLine2\tData";
        String encoded = TsvUtil.encode(content);

        assertNotNull(encoded);
        assertFalse(encoded.contains("\t"));
        assertFalse(encoded.contains("\n"));
    }

    @Test
    void testEncode_EmptyString() {
        String encoded = TsvUtil.encode("");
        assertNotNull(encoded);
    }

    @Test
    void testDecode_Valid() throws ApiException {
        String original = "Product1\t100\t50.00\nProduct2\t200\t75.50";
        String encoded = TsvUtil.encode(original);
        String decoded = TsvUtil.decode(encoded);

        assertEquals(original, decoded);
    }

    @Test
    void testDecode_InvalidBase64_ThrowsException() {
        String invalidBase64 = "This is not valid base64!@#$";

        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.decode(invalidBase64));
        assertEquals("Invalid base64 encoding", exception.getMessage());
    }

    @Test
    void testSplitLines_MultipleLines() throws ApiException {
        String content = "Line1\nLine2\nLine3";
        String[] lines = TsvUtil.splitLines(content);

        assertEquals(3, lines.length);
        assertEquals("Line1", lines[0]);
        assertEquals("Line2", lines[1]);
        assertEquals("Line3", lines[2]);
    }

    @Test
    void testSplitLines_SingleLine() throws ApiException {
        String content = "SingleLine";
        String[] lines = TsvUtil.splitLines(content);

        assertEquals(1, lines.length);
        assertEquals("SingleLine", lines[0]);
    }

    @Test
    void testSplitLines_Null_ThrowsException() {
        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.splitLines(null));
        assertEquals("Content is empty", exception.getMessage());
    }

    @Test
    void testSplitLines_Empty_ThrowsException() {
        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.splitLines(""));
        assertEquals("Content is empty", exception.getMessage());
    }

    @Test
    void testSplitLines_TooManyRows_ThrowsException() {
        // Create content with > 5000 lines
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5001; i++) {
            sb.append("Line").append(i).append("\n");
        }

        ApiException exception = assertThrows(ApiException.class,
                () -> TsvUtil.splitLines(sb.toString()));
        assertEquals("Maximum 5000 rows allowed", exception.getMessage());
    }

    @Test
    void testEncodeDecodeRoundTrip() throws ApiException {
        String original = "Barcode\tName\tPrice\nBC001\tProduct A\t100.50\nBC002\tProduct B\t200.75";
        String encoded = TsvUtil.encode(original);
        String decoded = TsvUtil.decode(encoded);

        assertEquals(original, decoded);
    }

    @Test
    void testEncodeDecodeRoundTrip_SpecialCharacters() throws ApiException {
        String original = "Special\tChars: !@#$%^&*()\nUnicode: ñ é ü";
        String encoded = TsvUtil.encode(original);
        String decoded = TsvUtil.decode(encoded);

        assertEquals(original, decoded);
    }

    @Test
    void testSplitLines_WindowsLineEndings() throws ApiException {
        String content = "Line1\r\nLine2\r\nLine3";
        String[] lines = TsvUtil.splitLines(content);

        assertEquals(3, lines.length);
        assertEquals("Line1", lines[0]);
        assertEquals("Line2", lines[1]);
        assertEquals("Line3", lines[2]);
    }
}
