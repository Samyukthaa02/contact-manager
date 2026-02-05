
package com.example.contactapp;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;
import com.example.contactapp.service.ContactService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Main {
    private static final String STORAGE = "data/contacts.csv";

    private static final String MENU = """
            Contact Manager (Java 21 demo)
            Commands:
              list                - list contacts
              add Name, email     - add contact
              find <id>           - find contact by id
              delete <id>         - delete contact by id
              m|menu              - show this menu
              q|quit|exit         - exit

            """;
    private static final String USAGE_ADD = """
            Usage:
              add Name, email@example.com

            """;

    public static void main(String[] args) {
        final ContactRepository repo = new ContactRepository(STORAGE);
        final ContactService service = new ContactService(repo);

        ThreadFactory virtualThreadFactory = Thread.ofVirtual().name("background-task-", 0).factory();
        var executor = Executors.newThreadPerTaskExecutor(virtualThreadFactory);

        executor.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10_000);
                    // simple heartbeat: load count and print
                    List<Contact> list = repo.findAll();
                    System.out.println("[Background] Stored contacts: " + list.size());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    break;
                } catch (Exception e) {
                    System.err.println("[Background] Error: " + e.getMessage());
                }
            }
        });

        Scanner scanner = new Scanner(System.in);
        try {
            printMenu();

            boolean running = true;
            while (running) {
                System.out.print("> ");
                String line = scanner.nextLine();
                if (line == null) break;

                Command command;
                try {
                    command = parse(line);
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid number: " + nfe.getMessage());
                    continue;
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    continue;
                }

                try {
                   switch (command) {
                        case QuitCmd qc -> {
                            System.out.println("Exiting...");
                            running = false;
                        }
                        case MenuCmd mc -> printMenu();
                        case ListCmd lc -> {
                            List<Contact> all = service.listContactsSortedByName();
                            if (all.isEmpty()) {
                                System.out.println("(no contacts)");
                            } else {
                                for (Contact c : all) {
                                    System.out.println(c.getId() + ": " + c.getName()
                                            + " <" + c.getEmail() + "> added:" + c.getCreatedAt());
                                }
                            }
                        }
                        case Add add -> {
                            Contact c = service.addContact(add.name, add.email);
                            System.out.println("Added: " + c);
                        }
                        case Find find -> {
                            Optional<Contact> opt = service.findById(find.id);
                            if (opt.isPresent()) {
                                System.out.println(opt.get());
                            } else {
                                System.out.println("Not found: " + find.id);
                            }
                        }
                        case DeleteCmd del -> {
                            boolean ok = service.delete(del.id);
                            System.out.println(ok ? ("Deleted " + del.id) : ("Not found " + del.id));
                        }
                        case UnknownCmd uc -> System.out.println("Unknown command. Type 'm' for menu.");
                    }
                } catch (NullPointerException npe) {
                    // Enhanced NPE details are automatic on JDK 14+; Java 8 will show standard message.
                    System.err.println("NPE: " + npe.getMessage());
                    Throwable cause = npe.getCause();
                    if (cause != null && cause instanceof IllegalStateException) {
                        System.err.println("Root cause (illegal state): " + cause.getMessage());
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        } finally {
            // Shutdown the executor gracefully
            executor.shutdownNow();
            scanner.close();
        }
    }

    private static void printMenu() {
        System.out.print(MENU);
    }

    private static Command parse(String line) {
        if (line == null || line.trim().isEmpty()) return new UnknownCmd("");

        String trimmed = line.trim();
        String lower = trimmed.toLowerCase();

        if ("q".equals(lower) || "quit".equals(lower) || "exit".equals(lower)) {
            return new QuitCmd();
        } else if ("m".equals(lower) || "menu".equals(lower)) {
            return new MenuCmd();
        } else if ("list".equals(lower)) {
            return new ListCmd();
        } else if (lower.startsWith("add")) {
            String payload = trimmed.substring(3).trim();
            String[] parts = payload.split(",", 2);
            if (parts.length < 2) {
                System.out.print(USAGE_ADD);
                return new UnknownCmd(line);
            } else {
                String name = parts[0].trim();
                String email = parts[1].trim();
                return new Add(name, email);
            }
        } else if (lower.startsWith("find")) {
            String payload = trimmed.substring(4).trim();
            int id = Integer.parseInt(payload);
            return new Find(id);
        } else if (lower.startsWith("delete")) {
            String payload = trimmed.substring(6).trim();
            int id = Integer.parseInt(payload);
            return new DeleteCmd(id);
        } else {
            return new UnknownCmd(line);
        }
    }

    // Convert to sealed interface and record classes
    sealed interface Command permits ListCmd, Add, Find, DeleteCmd, MenuCmd, QuitCmd, UnknownCmd {}

    record ListCmd() implements Command {}

    record Add(String name, String email) implements Command {}

    record Find(int id) implements Command {}

    record DeleteCmd(int id) implements Command {}

    record MenuCmd() implements Command {}

    record QuitCmd() implements Command {}

    record UnknownCmd(String raw) implements Command {}
}
