package com.example.contactapp.service;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service layer demonstrating Java 8-era idioms, now updated for Java 21.
 * - Uses modern Comparator.
 * - Adapts to Contact record.
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
        List<Contact> all = new ArrayList<>(repo.findAll());
        // Java 21 style using Comparator.comparing
        Collections.sort(all, Comparator.comparing(Contact::name, Comparator.nullsFirst(String::compareToIgnoreCase)));
        return all;
    }

    public Optional<Contact> findById(int id) {
        return repo.findById(id);
    }

    public boolean delete(int id) {
        return repo.delete(id);
    }
}