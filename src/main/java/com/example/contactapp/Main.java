
package com.example.contactapp;

import com.example.contactapp.model.Contact;
import com.example.contactapp.repo.ContactRepository;
import com.example.contactapp.service.ContactService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final String STORAGE = "data/contacts.csv";

    private static final String MENU =
            "Contact Manager (Java 8 demo)\n" +
            "Commands:\n" +
            "  list                - list contacts\n" +
            "  add Name, email     - add contact\n" +
            "  find <id>           - find contact by id\n" +
            "  delete <id>         - delete contact by id\n" +
            "  m|menu              - show this menu\n" +
            "  q|quit|exit         - exit\n" +
            "\n";
    private static final String USAGE_ADD =
            "Usage:\n" +
            "  add Name, email@example.com\n" +
            "\n";

    public static void main(String[] args) {
        final ContactRepository repo = new ContactRepository(STORAGE);
        final ContactService service = new ContactService(repo);

        Thread background = new Thread(() -> {
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
        background.start();

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
                   if (command instanceof QuitCmd) {
                        System.out.println("Exiting...");
                        running = false;

                    } else if (command instanceof MenuCmd) {
                        printMenu();

                    } else if (command instanceof ListCmd) {
                        List<Contact> all = service.listContactsSortedByName();
                        if (all.isEmpty()) {
                            System.out.println("(no contacts)");
                        } else {
                            for (Contact c : all) {
                                System.out.println(c.getId() + ": " + c.getName()
                                        + " <" + c.getEmail() + "> added:" + c.getCreatedAt());
                            }
                        }

                    } else if (command instanceof Add) {
                        Add add = (Add) command; // J21-upgrade: record pattern `case Add(String name, String email)`
                        Contact c = service.addContact(add.name, add.email);
                        System.out.println("Added: " + c);

                    } else if (command instanceof Find) {
                        Find find = (Find) command; // J21-upgrade: record pattern
                        Optional<Contact> opt = service.findById(find.id);
                        if (opt.isPresent()) {
                            System.out.println(opt.get());
                        } else {
                            System.out.println("Not found: " + find.id);
                        }

                    } else if (command instanceof DeleteCmd) {
                        DeleteCmd del = (DeleteCmd) command; // J21-upgrade: record pattern
                        boolean ok = service.delete(del.id);
                        System.out.println(ok ? ("Deleted " + del.id) : ("Not found " + del.id));

                    } else if (command instanceof UnknownCmd) {
                        System.out.println("Unknown command. Type 'm' for menu.");
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
            // Stop background thread politely
            background.interrupt();
            try {
                background.join(1000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
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


    static final class ListCmd implements Command { }

    static final class Add implements Command {
        final String name;
        final String email;
        Add(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    static final class Find implements Command {
        final int id;
        Find(int id) { this.id = id; }
    }

    static final class DeleteCmd implements Command {
        final int id;
        DeleteCmd(int id) { this.id = id; }
    }

    static final class MenuCmd implements Command { }

    static final class QuitCmd implements Command { }

    static final class UnknownCmd implements Command {
        final String raw;
        UnknownCmd(String raw) { this.raw = raw; }
    }
}
