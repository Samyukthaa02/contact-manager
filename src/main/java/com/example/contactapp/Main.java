package com.example.contactapp;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;
import com.example.contactapp.service.ContactService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Simple console application.
 *
 * Demonstrates:
 * - Scanner-based CLI
 * - Background thread implemented with anonymous Runnable (Java 8 style)
 *
 * Java 21 improvements:
 * - Replace background Thread with virtual threads / structured concurrency
 * - Replace Contact POJO with record
 * - Pattern matching and simplified Optional handling
 */
public class Main {
    private static final String STORAGE = "data/contacts.csv";

    public static void main(String[] args) {
        final ContactRepository repo = new ContactRepository(STORAGE);
        final ContactService service = new ContactService(repo);

        // Background saver thread (demonstration). In Java 21 you'd use virtual threads / scheduled executor improvements.
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
        background.setDaemon(true);

        Scanner scanner = new Scanner(System.in);
        printMenu();
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null) break;
            String cmd = line.trim().toLowerCase();
            try {
                if ("q".equals(cmd) || "exit".equals(cmd) || "quit".equals(cmd)) {
                    System.out.println("Exiting...");
                    break;
                } else if ("m".equals(cmd) || "menu".equals(cmd)) {
                    printMenu();
                } else if ("list".equals(cmd)) {
                    List<Contact> all = service.listContactsSortedByName();
                    if (all.isEmpty()) {
                        System.out.println("(no contacts)");
                    } else {
                        for (Contact c : all) {
                            System.out.println(c.id() + ": " + c.name() + " <" + c.email() + "> added:" + c.createdAt());
                        }
                    }
                } else if (cmd.startsWith("add")) {
                    // format: add Name, email@example.com
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
                } else if (cmd.startsWith("find")) {
                    String payload = line.substring(4).trim();
                    int id = Integer.parseInt(payload);
                    Optional<Contact> opt = service.findById(id);
                    // Java 21 style Optional handling
                    opt.ifPresentOrElse(
                        c -> System.out.println(c),
                        () -> System.out.println("Not found: " + id)
                    );
                } else if (cmd.startsWith("delete")) {
                    String payload = line.substring(6).trim();
                    int id = Integer.parseInt(payload);
                    boolean ok = service.delete(id);
                    System.out.println(ok ? ("Deleted " + id) : ("Not found " + id));
                } else {
                    System.out.println("Unknown command. Type 'm' for menu.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }

        // Stop background thread politely
        background.interrupt();
        try {
            background.join(1000);
        } catch (InterruptedException ignored) { }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("Contact Manager (Java 8 demo)");
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