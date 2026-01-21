package com.example.contactapp.service;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service layer demonstrating Java 8-era idioms.
 * - Uses anonymous Comparator class
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
        List<Contact> all = new ArrayList<>(repo.findAll());
        // Java 8 style anonymous Comparator. In Java 21 you'd likely use Comparator.comparing(Contact::getName)
        Collections.sort(all, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                if (o1.getName() == null && o2.getName() == null) return 0;
                if (o1.getName() == null) return -1;
                if (o2.getName() == null) return 1;
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return all;
    }

    public Optional<Contact> findById(int id) {
        return repo.findById(id);
    }

    public boolean delete(int id) {
        return repo.delete(id);
    }
}