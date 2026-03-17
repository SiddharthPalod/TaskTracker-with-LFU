package tasktracker.cache;

import tasktracker.model.Status;
import tasktracker.model.Task;

import java.util.Date;

public class TaskCacheSelfTest {
    public static void main(String[] args) {
        TaskCache cache = new TaskCache(2);

        Task t1 = mk(1, "a");
        Task t2 = mk(2, "b");
        Task t3 = mk(3, "c");

        cache.put(t1);
        cache.put(t2);
        cache.get(1);
        cache.get(1);
        cache.put(t3);

        if (cache.get(2) != null) {
            throw new RuntimeException("Expected key 2 to be evicted (LFU).");
        }
        if (cache.get(1) == null || cache.get(3) == null) {
            throw new RuntimeException("Expected keys 1 and 3 to exist.");
        }

        System.out.println("OK");
    }

    private static Task mk(int id, String desc) {
        Task t = new Task();
        t.id = id;
        t.description = desc;
        t.status = Status.TODO;
        t.createdAt = new Date();
        t.updatedAt = new Date();
        return t;
    }
}

