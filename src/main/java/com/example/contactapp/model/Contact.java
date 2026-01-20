package com.example.contactapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Plain old Java object (POJO) for Contact.
 * In Java 21 this could be a record: `public record Contact(int id, String name, String email, LocalDateTime createdAt) { }`
 */
public class Contact implements Serializable {
    private int id;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Contact() {
    }

    public Contact(int id, String name, String email, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    } 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    } 

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    } 

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;
        return id == contact.id &&
                Objects.equals(name, contact.name) &&
                Objects.equals(email, contact.email) &&
                Objects.equals(createdAt, contact.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, createdAt);
    }

    @Override
    public String toString() {
        return "Contact{id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' +
                ", createdAt=" + createdAt + '}';
    }
}