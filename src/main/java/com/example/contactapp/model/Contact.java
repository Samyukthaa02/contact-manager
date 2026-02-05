package com.example.contactapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record Contact(
        int id,
        String name,
        String email,
        LocalDateTime createdAt
) implements Serializable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Contact {
        // Compact constructor for validation and normalization
        name = (name == null) ? "" : name.trim();
        email = (email == null) ? "" : email.trim();
        createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
    }

    // Default constructor for cases where ID is not yet known or to be generated (e.g., for new contacts)
    public Contact(String name, String email) {
        this(0, name, email, null);
    }

    public String toCsvLine() {
        return id + "," + escape(name) + "," + escape(email) + "," + createdAt.format(FORMATTER);
    }

    public static Contact fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;

        String[] parts = line.split(",", 4);
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
}
