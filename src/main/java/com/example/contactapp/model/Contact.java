package com.example.contactapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects; // Keep if you have custom equals/hashCode beyond record's default

/**
 * Record for Contact, replacing the old POJO.
 */
public record Contact(int id, String name, String email, LocalDateTime createdAt) implements Serializable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Compact constructor (optional, for validation or normalization)
    public Contact {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
    }

    public String toCsvLine() {
        // CSV escaping is minimal for brevity
        return id + "," + escape(name) + "," + escape(email) + "," + createdAt.format(FORMATTER);
    }

    public static Contact fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(",", 4);
        if (parts.length < 4) {
            return null;
        }
        int id = Integer.parseInt(parts[0]);
        String name = unescape(parts[1]);
        String email = unescape(parts[2]);
        LocalDateTime createdAt = LocalDateTime.parse(parts[3], FORMATTER);
        return new Contact(id, name, email, createdAt);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\n", "\\n").replace(",", "\\,");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\,", ",");
    }

    // Records automatically provide equals(), hashCode(), and toString() based on their components.
    // So the explicit overrides are no longer needed.
    // Getters are also automatically generated (e.g., id(), name(), etc.)
}