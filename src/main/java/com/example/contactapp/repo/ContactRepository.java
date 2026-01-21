package com.example.contactapp.repo;

import com.example.contactapp.model.Contact;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File-backed repository storing contacts as CSV.
 * Uses Java 8 APIs (Files, Paths, try-with-resources).
 */
public class ContactRepository {
    private final Path file;

    public ContactRepository(String filePath) {
        this.file = Paths.get(filePath);
        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize storage file", e);
        }
    }

    public synchronized List<Contact> findAll() {
        List<Contact> out = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                Contact c = Contact.fromCsvLine(line);
                if (c != null) {
                    out.add(c);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading contacts", e);
        }
        return out;
    }

    public synchronized Optional<Contact> findById(int id) {
        List<Contact> all = findAll();
        for (Contact c : all) {
            if (c.getId() == id) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public synchronized void save(Contact contact) {
        List<Contact> all = findAll();
        boolean replaced = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == contact.getId()) {
                all.set(i, contact);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            all.add(contact);
        }
        writeAll(all);
    }

    public synchronized boolean delete(int id) {
        List<Contact> all = findAll();
        boolean removed = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == id) {
                all.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            writeAll(all);
        }
        return removed;
    }

    public synchronized int nextId() {
        int max = 0;
        for (Contact c : findAll()) {
            if (c.getId() > max) max = c.getId();
        }
        return max + 1;
    }

    private void writeAll(List<Contact> all) {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Contact c : all) {
                w.write(c.toCsvLine());
                w.newLine();
            }
            w.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error writing contacts", e);
        }
    }

    /**
     * Convenience to add a new contact with id generation and createdAt now.
     */
    public synchronized Contact addNew(String name, String email) {
        int id = nextId();
        Contact c = new Contact(id, name, email, LocalDateTime.now());
        save(c);
        return c;
    }
}