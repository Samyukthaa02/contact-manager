package com.example.contactapp.service;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Added for Collectors

/**
 * Service layer demonstrating Java 21 idioms.
 */
public class ContactService {
    private final ContactRepository repo;

    public ContactService(ContactRepository repo) {
        this.repo = repo;
    }

    public Contact addContact(String name, String email) {
        return repo.addNew(name, email);
    }

    public List<Contact> listContactsSortedByName() {
        // Using Java 21 Stream API with Comparator.comparing and nullsFirst
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Contact::name, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    public Optional<Contact> findById(int id) {
        return repo.findById(id);
    }

    public boolean delete(int id) {
        return repo.delete(id);
    }
}