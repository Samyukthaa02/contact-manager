package com.example.contactapp.service;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer demonstrating Java 8-era idioms.
 * - Uses anonymous Comparator class (replaced with modern Comparator.comparing)
 * - Uses Optional with isPresent/get (could be improved with functional style)
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
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Contact::name, Comparator.nullsFirst(String::compareToIgnoreCase)))
                .collect(Collectors.toUnmodifiableList()); // Using toUnmodifiableList for immutable result
    }

    public Optional<Contact> findById(int id) {
        return repo.findById(id);
    }

    public boolean delete(int id) {
        return repo.delete(id);
    }
}
