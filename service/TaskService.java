package tasktracker.service;

import java.util.HashMap;
import java.util.Map;

import tasktracker.cache.TaskCache;
import tasktracker.model.Status;
import tasktracker.model.Task;
import tasktracker.storage.JsonStorage;

public class TaskService {

    TaskCache cache;
    JsonStorage storage;
    private final Map<Integer, Task> tasks;

    public TaskService(TaskCache cache, JsonStorage storage) throws Exception {
        this.cache = cache;
        this.storage = storage;
        this.tasks = new HashMap<>(storage.loadTasks());
    }

    public boolean addTask(Task t) throws Exception{
        if (t == null) return false;
        String desc = (t.description == null) ? "" : t.description.trim();
        if (desc.isEmpty()) return false;

        String needle = desc.toLowerCase();
        for (Task existing : tasks.values()) {
            if (existing == null || existing.description == null) continue;
            if (existing.description.trim().toLowerCase().equals(needle)) {
                return false;
            }
        }

        t.description = desc;
        tasks.put(t.id, t);
        cache.put(t);
        storage.rewriteFile(tasks);
        return true;
    }

    public boolean deleteTask(int id) throws Exception{
        Task task = tasks.get(id);
        if (task == null) {
            return false;
        }
        tasks.remove(id);
        cache.remove(id);
        storage.rewriteFile(tasks);
        return true;
    }

    public boolean updateTask(int id, String description) throws Exception {
        Task task = tasks.get(id);
        if (task == null) {
            return false;
        }
        task.description = description;
        task.updatedAt = new java.util.Date();
        
        cache.put(task);
        storage.rewriteFile(tasks);
        return true;
    }

    public boolean updateStatus(int id, Status status) throws Exception {
        Task task = tasks.get(id);
        if (task == null) return false;
        task.status = status;
        task.updatedAt = new java.util.Date();
        cache.put(task);
        storage.rewriteFile(tasks);
        return true;
    }

    public Task getTask(int id) {
        Task t = cache.get(id);
        if (t != null) return t;
        t = tasks.get(id);
        if (t != null) cache.put(t);
        return t;
    }

    public void listTasks() {
        for (Task task : tasks.values()) {
            printTask(task);
        }
    }

    public void listTasks(Status status) {
        for (Task task : tasks.values()) {
            if (task.status == status) {
                printTask(task);
            }
        }
    }

    public void listTasksNotDone() {
        for (Task task : tasks.values()) {
            if (task.status != Status.DONE) {
                printTask(task);
            }
        }
    }

    public int nextId() {
        return tasks.keySet().stream().max(Integer::compare).orElse(0) + 1;
    }

    private void printTask(Task task) {
        System.out.println(
            "ID: " + task.id +
            " | " + task.description +
            " | " + task.status
        );
    }
}