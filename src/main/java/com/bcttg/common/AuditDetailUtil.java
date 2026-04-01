package com.bcttg.common;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuditDetailUtil {
    private AuditDetailUtil() {
    }

    public static String encode(Map<String, String> values) {
        return values.entrySet().stream()
            .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
            .map(entry -> sanitize(entry.getKey()) + "=" + sanitize(entry.getValue()))
            .reduce((left, right) -> left + ";" + right)
            .orElse(null);
    }

    public static Map<String, String> decode(String detail) {
        Map<String, String> values = new LinkedHashMap<>();
        if (detail == null || detail.isBlank()) {
            return values;
        }
        String[] pairs = detail.split(";");
        for (String pair : pairs) {
            int separatorIndex = pair.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }
            String key = pair.substring(0, separatorIndex).trim();
            String value = pair.substring(separatorIndex + 1).trim();
            if (!key.isBlank()) {
                values.put(key, value);
            }
        }
        return values;
    }

    private static String sanitize(String value) {
        return value.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }
}
