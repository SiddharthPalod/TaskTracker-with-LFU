package tasktracker.cache;
import tasktracker.model.Task;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class TaskCache {
    private final int capacity;

    private final Map<Integer, Task> values = new HashMap<>();
    private final Map<Integer, Integer> freqs = new HashMap<>();
    private final Map<Integer, LinkedHashSet<Integer>> lists = new HashMap<>();
    private int minFreq = 0;

    public TaskCache(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("capacity must be >= 0");
        this.capacity = capacity;
    }

    public TaskCache() {
        this(50);
    }

    public void put(Task task){
        if (task == null) return;
        if (capacity == 0) return;

        int key = task.id;

        if (values.containsKey(key)) {
            values.put(key, task);
            touch(key);
            return;
        }

        if (values.size() >= capacity) {
            evictOne();
        }

        values.put(key, task);
        freqs.put(key, 1);
        lists.computeIfAbsent(1, _k -> new LinkedHashSet<>()).add(key);
        minFreq = 1;
    }

    public Task get(int id){
        Task t = values.get(id);
        if (t == null) return null;
        touch(id);
        return t;
    }

    public void remove(int id){
        if (!values.containsKey(id)) return;
        int f = freqs.getOrDefault(id, 0);
        values.remove(id);
        freqs.remove(id);
        LinkedHashSet<Integer> set = lists.get(f);
        if (set != null) {
            set.remove(id);
            if (set.isEmpty()) {
                lists.remove(f);
                if (minFreq == f) {
                    recomputeMinFreq();
                }
            }
        }
    }

    public Map<Integer, Task>getAll(){
        return values;
    }

    public int nextId() {
        return values.keySet().stream().max(Integer::compare).orElse(0) + 1;
    }

    public int size() {
        return values.size();
    }

    public int capacity() {
        return capacity;
    }

    private void touch(int key) {
        int f = freqs.getOrDefault(key, 0);
        if (f == 0) return;

        LinkedHashSet<Integer> oldSet = lists.get(f);
        if (oldSet != null) {
            oldSet.remove(key);
            if (oldSet.isEmpty()) {
                lists.remove(f);
                if (minFreq == f) minFreq = f + 1;
            }
        }

        int nf = f + 1;
        freqs.put(key, nf);
        lists.computeIfAbsent(nf, _k -> new LinkedHashSet<>()).add(key);
    }

    private void evictOne() {
        LinkedHashSet<Integer> set = lists.get(minFreq);
        if (set == null || set.isEmpty()) {
            recomputeMinFreq();
            set = lists.get(minFreq);
            if (set == null || set.isEmpty()) return;
        }

        Integer evictKey = set.iterator().next(); // LRU within same freq
        set.remove(evictKey);
        if (set.isEmpty()) {
            lists.remove(minFreq);
        }
        values.remove(evictKey);
        freqs.remove(evictKey);
        if (!lists.containsKey(minFreq)) {
            recomputeMinFreq();
        }
    }

    private void recomputeMinFreq() {
        int min = Integer.MAX_VALUE;
        for (Map.Entry<Integer, LinkedHashSet<Integer>> e : lists.entrySet()) {
            if (!e.getValue().isEmpty() && e.getKey() < min) min = e.getKey();
        }
        minFreq = (min == Integer.MAX_VALUE) ? 0 : min;
    }
}
