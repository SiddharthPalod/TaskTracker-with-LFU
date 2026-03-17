package tasktracker;

import java.util.Date;
import java.util.Scanner;

import tasktracker.cache.TaskCache;
import tasktracker.model.*;
import tasktracker.service.TaskService;
import tasktracker.storage.JsonStorage;

public class Main {

    public static void main(String[] args) throws Exception {

        TaskCache cache = new TaskCache(50);
        JsonStorage storage = new JsonStorage();
        TaskService service = new TaskService(cache, storage);

        if (args != null && args.length > 0) {
            runCommand(service, args);
            return;
        }

        Scanner scanner = new Scanner(System.in);

        System.out.println("Task Tracker CLI");
        System.out.println("Type 'help' to see commands");

        while (true) {

            System.out.print("> ");
            String input = scanner.nextLine();
            String[] cmd = tokenize(input);
            if (cmd.length == 0) continue;

            if (!runCommand(service, cmd)) {
                scanner.close();
                return;
            }
        }
    }

    private static boolean runCommand(TaskService service, String[] cmd) throws Exception {
        String command = cmd[0];

        switch (command) {

            case "add":
                if (cmd.length < 2) {
                    System.out.println("Description required");
                    return true;
                }

                Task task = new Task();
                task.id = service.nextId();
                task.description = cmd[1];
                task.status = Status.TODO;
                task.createdAt = new Date();
                task.updatedAt = new Date();

                if (service.addTask(task)) {
                    System.out.println("Task added successfully (ID: " + task.id + ")");
                } else {
                    System.out.println("Duplicate (or empty) task description. Not added.");
                }
                return true;

            case "update":
                if (cmd.length < 3) {
                    System.out.println("Usage: update <id> <description>");
                    return true;
                }

                if (service.updateTask(Integer.parseInt(cmd[1]), cmd[2])) {
                    System.out.println("Task updated successfully");
                } else {
                    System.out.println("Task not found");
                }
                return true;

            case "delete":
                if (cmd.length < 2) {
                    System.out.println("Usage: delete <id>");
                    return true;
                }

                if (service.deleteTask(Integer.parseInt(cmd[1]))) {
                    System.out.println("Task deleted successfully");
                } else {
                    System.out.println("Task not found");
                }
                return true;

            case "mark-in-progress":
                if (cmd.length < 2) {
                    System.out.println("Usage: mark-in-progress <id>");
                    return true;
                }
                if (!service.updateStatus(Integer.parseInt(cmd[1]), Status.IN_PROGRESS)) {
                    System.out.println("Task not found");
                } else {
                    System.out.println("Task marked in progress");
                }
                return true;

            case "mark-done":
                if (cmd.length < 2) {
                    System.out.println("Usage: mark-done <id>");
                    return true;
                }
                if (!service.updateStatus(Integer.parseInt(cmd[1]), Status.DONE)) {
                    System.out.println("Task not found");
                } else {
                    System.out.println("Task marked done");
                }
                return true;

            case "list":
                if (cmd.length == 1) {
                    service.listTasks();
                } else {
                    String raw = cmd[1].trim();
                    String normalized = raw.replace('-', '_').toUpperCase();
                    if (normalized.equals("NOT_DONE")) {
                        service.listTasksNotDone();
                        return true;
                    }
                    Status status = Status.valueOf(normalized);
                    service.listTasks(status);
                }
                return true;

            case "help":
                printHelp();
                return true;

            case "exit":
                System.out.println("Exiting...");
                return false;

            default:
                System.out.println("Unknown command");
                printHelp();
                return true;
        }
    }

    private static void printHelp() {
        System.out.println("\nTask Tracker CLI");
        System.out.println("----------------------------");
        System.out.println("Usage (positional args): java -cp out tasktracker.Main <command> [args]");
        System.out.println("add \"description\"          Add a new task");
        System.out.println("update <id> \"description\"  Update task description");
        System.out.println("delete <id>                 Delete a task");
        System.out.println("mark-in-progress <id>       Mark task as in progress");
        System.out.println("mark-done <id>              Mark task as done");
        System.out.println("list                        List all tasks");
        System.out.println("list todo                   List TODO tasks");
        System.out.println("list in-progress            List IN_PROGRESS tasks");
        System.out.println("list done                   List DONE tasks");
        System.out.println("list not-done               List tasks that are not DONE");
        System.out.println("help                        Show this help message");
        System.out.println("exit                        Exit the program\n");
    }

    private static String[] tokenize(String input) {
        if (input == null) return new String[0];
        input = input.trim();
        if (input.isEmpty()) return new String[0];

        java.util.ArrayList<String> tokens = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                continue;
            }
            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                continue;
            }

            if (!inSingle && !inDouble && Character.isWhitespace(c)) {
                if (cur.length() > 0) {
                    tokens.add(cur.toString());
                    cur.setLength(0);
                }
                continue;
            }

            cur.append(c);
        }

        if (cur.length() > 0) tokens.add(cur.toString());
        return tokens.toArray(new String[0]);
    }
}