package com.example.contactapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Plain old Java object (POJO) for Contact.
 * In Java 21 this could be a record: `public record Contact(int id, String name, String email, LocalDateTime createdAt) { }`
 */
public record Contact(int id, String name, String email, LocalDateTime createdAt) implements Serializable {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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
}