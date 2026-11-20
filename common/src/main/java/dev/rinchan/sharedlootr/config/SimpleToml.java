package dev.rinchan.sharedlootr.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal zero-dependency TOML key/value parser and formatter for flat configuration files.
 */
public final class SimpleToml {
    private SimpleToml() {
    }

    public static Map<String, String> parse(String content) {
        Map<String, String> entries = new LinkedHashMap<>();
        if (content == null || content.isEmpty()) {
            return entries;
        }

        String[] lines = content.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = stripComment(rawLine).trim();
            if (line.isEmpty() || line.startsWith("[")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = unquote(line.substring(eq + 1).trim());
            if (!key.isEmpty()) {
                entries.put(key, value);
            }
        }
        return entries;
    }

    public static boolean getBoolean(Map<String, String> entries, String key, boolean fallback) {
        String raw = entries.get(key);
        if (raw == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }
        return fallback;
    }

    public static String getString(Map<String, String> entries, String key, String fallback) {
        String raw = entries.get(key);
        return raw != null && !raw.isBlank() ? raw : fallback;
    }

    public static String quote(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private static String stripComment(String line) {
        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\' && inDoubleQuotes) {
                escaped = true;
                continue;
            }
            if (c == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (c == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '#' && !inDoubleQuotes && !inSingleQuotes) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if (first == '"' && last == '"') {
                String inner = value.substring(1, value.length() - 1);
                return inner
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
            if (first == '\'' && last == '\'') {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
