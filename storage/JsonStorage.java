package tasktracker.storage;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tasktracker.model.Task;

public class JsonStorage {

    private final String filePath;

    public JsonStorage() {
        this("data/tasks.ndjson");
    }

    public JsonStorage(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("filePath must not be empty");
        }
        this.filePath = filePath;
    }

    private void ensureFileExists() throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            file.getParentFile().mkdirs(); 
            file.createNewFile();        
        }
    }

    public void appendTask(Task task) throws Exception {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(task.toJson());
            writer.newLine();
        }
    }

    public Map<Integer, Task> loadTasks() throws Exception {
        ensureFileExists();
        Map<Integer, Task> tasks = new ConcurrentHashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Task task = Task.fromJson(line);
                tasks.put(task.id, task);
            }
        }

        return tasks;
    }

    public void rewriteFile(Map<Integer, Task> tasks) throws Exception {
        ensureFileExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Task task : tasks.values()) {
                writer.write(task.toJson());
                writer.newLine();
            }
        }
    }
}