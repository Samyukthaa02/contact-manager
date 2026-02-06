package com.example.contactapp.repo;

import com.example.contactapp.model.Contact;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class ContactRepository {
    private final Path file;
    private final AtomicInteger nextId = new AtomicInteger(0);

    public ContactRepository() {
        this.file = Paths.get("data/contacts.csv");
        initializeStorage();
        loadNextId();
    }

    private void initializeStorage() {
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

    private void loadNextId() {
        nextId.set(findAll().stream().mapToInt(Contact::id).max().orElse(0) + 1);
    }

    public synchronized List<Contact> findAll() {
        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return lines.map(Contact::fromCsvLine)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            throw new RuntimeException("Error reading contacts", e);
        }
    }

    public synchronized Optional<Contact> findById(int id) {
        return findAll().stream()
                .filter(c -> c.id() == id)
                .findFirst();
    }

    public synchronized void save(Contact contact) {
        List<Contact> all = findAll();
        boolean replaced = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id() == contact.id()) {
                all.set(i, contact);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            all.add(contact);
        }
        writeAll(all);
        loadNextId(); // Recalculate nextId after save
    }

    public synchronized boolean delete(int id) {
        List<Contact> all = findAll();
        boolean removed = all.removeIf(c -> c.id() == id);
        if (removed) {
            writeAll(all);
            loadNextId(); // Recalculate nextId after delete
        }
        return removed;
    }

    public synchronized int nextId() {
        return nextId.getAndIncrement();
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
        Contact c = new Contact(id, name, email, LocalDateTime.now());
        save(c);
        return c;
    }
}
