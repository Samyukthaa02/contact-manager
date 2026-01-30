package com.example.contactapp;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;
import com.example.contactapp.service.ContactService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final String STORAGE = "data/contacts.csv";

    public static void main(String[] args) {
        final ContactRepository repo = new ContactRepository(STORAGE);
        final ContactService service = new ContactService(repo);

        // Using Virtual Threads for the background saver
        // In Java 21, Executors.newVirtualThreadPerTaskExecutor() provides a simple way to create virtual threads.
        // For a single long-running task, Thread.startVirtualThread is even more direct.
        Thread background = Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(10_000);
                    // simple heartbeat: load count and print
                    List<Contact> list = repo.findAll();
                    System.out.println("[Background] Stored contacts: " + list.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("[Background] Error: " + e.getMessage());
                }
            }
        });

        Scanner scanner = new Scanner(System.in);
        printMenu();
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null) break;
            String cmd = line.trim().toLowerCase();
            try {
                // Using switch expression for command handling
                switch (cmd) {
                    case "q", "exit", "quit" -> {
                        System.out.println("Exiting...");
                        // Stop background thread politely
                        background.interrupt();
                        try {
                            background.join(1000);
                        } catch (InterruptedException ignored) { /* ignore */ }
                        scanner.close();
                        return; // Exit main method
                    }
                    case "m", "menu" -> printMenu();
                    case "list" -> {
                        List<Contact> all = service.listContactsSortedByName();
                        if (all.isEmpty()) {
                            System.out.println("(no contacts)");
                        } else {
                            // Enhanced for loop with implicit type for Contact record
                            for (var c : all) { // 'var' can be used with records
                                System.out.println(c.id() + ": " + c.name() + " <" + c.email() + "> added:" + c.createdAt());
                            }
                        }
                    }
                    case String s when s.startsWith("add") -> {
                        String payload = line.substring(3).trim();
                        String[] parts = payload.split(",", 2);
                        if (parts.length < 2) {
                            System.out.println("Usage: add Name, email@example.com");
                        } else {
                            String name = parts[0].trim();
                            String email = parts[1].trim();
                            Contact c = service.addContact(name, email);
                            System.out.println("Added: " + c);
                        }
                    }
                    case String s when s.startsWith("find") -> {
                        String payload = line.substring(4).trim();
                        int id = Integer.parseInt(payload);
                        Optional<Contact> opt = service.findById(id);
                        // Using Optional.ifPresentOrElse for modern Optional handling
                        opt.ifPresentOrElse(
                                System.out::println,
                                () -> System.out.println("Not found: " + id)
                        );
                    }
                    case String s when s.startsWith("delete") -> {
                        String payload = line.substring(6).trim();
                        int id = Integer.parseInt(payload);
                        boolean ok = service.delete(id);
                        System.out.println(ok ? ("Deleted " + id) : ("Not found " + id));
                    }
                    default -> System.out.println("Unknown command. Type 'm' for menu.");
                }
            } catch (NumberFormatException e) {
                 System.err.println("Error: Invalid ID format. " + e.getMessage());
            }
            catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("Contact Manager (Java 21)"); // Updated version in menu
        System.out.println("Commands:");
        System.out.println("  list                - list contacts");
        System.out.println("  add Name, email     - add contact");
        System.out.println("  find <id>           - find contact by id");
        System.out.println("  delete <id>         - delete contact by id");
        System.out.println("  m|menu              - show this menu");
        System.out.println("  q|quit|exit         - exit");
        System.out.println();
    }
}