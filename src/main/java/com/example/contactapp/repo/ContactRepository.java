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
import java.util.stream.Collectors; // Added for Collectors

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
        return findAll().stream()
                .filter(contact -> contact.id() == id) // Using record accessor contact.id()
                .findFirst();
    }

    public synchronized void save(Contact contact) {
        List<Contact> all = findAll();
        // Check if contact already exists and update, otherwise add
        boolean replaced = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id() == contact.id()) { // Using record accessor contact.id()
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
        List<Contact> initialContacts = findAll();
        List<Contact> remainingContacts = initialContacts.stream()
                .filter(contact -> contact.id() != id) // Using record accessor contact.id()
                .collect(Collectors.toList());

        if (remainingContacts.size() < initialContacts.size()) {
            writeAll(remainingContacts);
            return true;
        }
        return false;
    }

    public synchronized int nextId() {
        return findAll().stream()
                .mapToInt(Contact::id) // Using record accessor Contact::id
                .max()
                .orElse(0) + 1;
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


    public synchronized Contact addNew(String name, String email) {
        int id = nextId();
        // The Contact record constructor automatically handles null/trim for name and email, and sets createdAt if null
        Contact c = new Contact(id, name, email, null);
        save(c);
        return c;
    }
}