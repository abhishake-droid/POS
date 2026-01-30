package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import java.util.Base64;

public class TsvUtil {

    public static String decode(String base64Content) throws ApiException {
        try {
            return new String(Base64.getDecoder().decode(base64Content));
        } catch (IllegalArgumentException e) {
            throw new ApiException("Invalid base64 encoding");
        }
    }

    public static String[] splitLines(String content) throws ApiException {
        if (content == null || content.isEmpty()) {
            throw new ApiException("Content is empty");
        }
        String[] lines = content.split("\\r?\\n");
        if (lines.length > 5000) {
            throw new ApiException("Maximum 5000 rows allowed");
        }
        return lines;
    }

    public static String encode(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }
}
