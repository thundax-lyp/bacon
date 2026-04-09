package com.github.thundax.bacon.order.application.codec;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OrderOutboxPayloadCodec {

    private OrderOutboxPayloadCodec() {
    }

    public static String encode(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encodePart(entry.getKey())).append('=')
                    .append(encodePart(entry.getValue() == null ? "" : entry.getValue()));
        }
        return builder.toString();
    }

    public static Map<String, String> decode(String payload) {
        Map<String, String> result = new LinkedHashMap<>();
        if (payload == null || payload.isBlank()) {
            return result;
        }
        String[] pairs = payload.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = decodePart(parts[0]);
            String value = parts.length > 1 ? decodePart(parts[1]) : "";
            result.put(key, value);
        }
        return result;
    }

    private static String encodePart(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decodePart(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
