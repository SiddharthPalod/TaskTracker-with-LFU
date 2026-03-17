package tasktracker.bench;

import tasktracker.cache.TaskCache;
import tasktracker.model.Status;
import tasktracker.model.Task;
import tasktracker.service.TaskService;
import tasktracker.storage.JsonStorage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CachePerfBenchmark {
    public static void main(String[] args) throws Exception {
        int nTasks = intArg(args, 0, 20_000);
        int nLookups = intArg(args, 1, 200_000);
        int cacheCap = intArg(args, 2, 200);
        String file = (args != null && args.length >= 4) ? args[3] : "data/bench_tasks.ndjson";

        System.out.println("Dataset: tasks=" + nTasks + ", lookups=" + nLookups + ", cacheCap=" + cacheCap);
        System.out.println("Benchmark file: " + file);

        JsonStorage benchStorage = new JsonStorage(file);
        Map<Integer, Task> seed = new HashMap<>(nTasks * 2);
        Date now = new Date();
        for (int i = 1; i <= nTasks; i++) {
            Task t = new Task();
            t.id = i;
            t.description = "task-" + i;
            t.status = Status.TODO;
            t.createdAt = now;
            t.updatedAt = now;
            seed.put(i, t);
        }
        benchStorage.rewriteFile(seed);

        int[] ids = new int[nLookups];
        Random r = new Random(1234567);
        for (int i = 0; i < nLookups; i++) {
            boolean hot = r.nextInt(10) < 8;
            int bound = hot ? Math.max(1, nTasks / 10) : nTasks;
            ids[i] = 1 + r.nextInt(bound);
        }

        // A) Baseline
        long tA0 = System.nanoTime();
        long checksumA = 0;
        for (int id : ids) {
            Map<Integer, Task> all = benchStorage.loadTasks();
            Task t = all.get(id);
            if (t != null) checksumA += t.id;
        }
        long tA1 = System.nanoTime();

        // B) LFU cache
        TaskCache cache = new TaskCache(cacheCap);
        TaskService service = new TaskService(cache, benchStorage);
        long tB0 = System.nanoTime();
        long checksumB = 0;
        for (int id : ids) {
            Task t = service.getTask(id);
            if (t != null) checksumB += t.id;
        }
        long tB1 = System.nanoTime();

        double msA = (tA1 - tA0) / 1_000_000.0;
        double msB = (tB1 - tB0) / 1_000_000.0;
        System.out.println();
        System.out.println("A) JSON load per lookup: " + fmt(msA) + " ms  (checksum " + checksumA + ")");
        System.out.println("B) LFU cache getTask(): " + fmt(msB) + " ms  (checksum " + checksumB + ")");
        System.out.println("Speedup: " + fmt(msA / msB) + "x");
    }

    private static int intArg(String[] args, int idx, int def) {
        if (args == null || args.length <= idx) return def;
        try {
            return Integer.parseInt(args[idx]);
        } catch (Exception e) {
            return def;
        }
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }
}

