package com.example.contactapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// Converted to Java Record
public record Contact(int id, String name, String email, LocalDateTime createdAt) implements Serializable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Compact constructor for validation and default values
    public Contact {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim();
        createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public String toCsvLine() {
        // Using Text Blocks for better readability if desired, but not strictly necessary here.
        // Sticking to original string concatenation for consistency with escape/unescape.
        return id + "," + escape(name) + "," + escape(email) + "," + createdAt.format(FORMATTER);
    }

    public static Contact fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;

        String[] parts = line.split(",", 4);
        // Java 21 switch expression
        return switch (parts.length) {
            case 4 -> {
                int id = Integer.parseInt(parts[0]);
                String name = unescape(parts[1]);
                String email = unescape(parts[2]);
                LocalDateTime createdAt = LocalDateTime.parse(parts[3], FORMATTER);
                yield new Contact(id, name, email, createdAt);
            }
            default -> null;
        };
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\n", "\\n").replace(",", "\\,");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\,", ",");
    }

    // equals, hashCode, and toString are automatically generated for records.
    // The original equals had pattern matching for instanceof, which is now inherent.
}