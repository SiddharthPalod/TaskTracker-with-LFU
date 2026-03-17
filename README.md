# Task Tracker CLI (Java)
A simple command-line task tracker that stores tasks in **NDJSON** (`data/tasks.ndjson`) using **no external libraries**.

## Requirements checklist (from `Requirements.md`)
From the project : https://roadmap.sh/projects/task-tracker
- **Add / Update / Delete tasks**:
- **Mark in progress / done**: 
- **List all tasks**: 
- **List tasks by status (todo / in-progress / done)**: (`list todo`, `list in-progress`, `list done`)
- **List tasks that are not done**: (`list not-done`)
- **Store tasks in a JSON file**: (`data/tasks.ndjson`, auto-created)
- **Use positional arguments in command line**: (also supports interactive mode)
- **No external libraries / frameworks**: 

## What’s new beyond the base requirements

- **LFU cache (bounded hot set)**: `TaskCache` is a real **LFU cache** with a fixed capacity and tie-break by recency.
  - Storage is **authoritative** (all tasks persist).
  - Cache holds only “hot” tasks; eviction does **not** delete tasks from disk.
- **Robust JSON handling for descriptions**:
  - Descriptions can contain commas/quotes/colons/backslashes without breaking parsing.
- **Quoted multi-word CLI input**:
  - `add "multi word task"` and `update 2 'multi word task'` work (single or double quotes).
- **Duplicate description prevention**:
  - `add` refuses a task if the description (trimmed, case-insensitive) already exists.

## Build

From the project root:

```bash
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
```

## Run

### Positional-args mode (recommended)

```bash
java -cp out tasktracker.Main add "Buy groceries and cook dinner"
java -cp out tasktracker.Main update 1 "Buy groceries + cook dinner"
java -cp out tasktracker.Main mark-in-progress 1
java -cp out tasktracker.Main mark-done 1
java -cp out tasktracker.Main list
java -cp out tasktracker.Main list todo
java -cp out tasktracker.Main list in-progress
java -cp out tasktracker.Main list done
java -cp out tasktracker.Main list not-done
```

### Interactive mode (REPL)

```bash
java -cp out tasktracker.Main
```

Then type commands like:

```text
add "Write documentation"
update 2 "Fix cache eviction"
list not-done
exit
```

## Proof it works

### LFU eviction correctness

There’s a small self-test that asserts LFU eviction behavior:

```bash
java -cp out tasktracker.cache.TaskCacheSelfTest
```

Expected output:

```text
OK
```

### Performance improvement (time-based benchmark)

Benchmark compares:

- **A) JSON load per lookup**: reads/parses the NDJSON file on every `get` (worst-case “no cache”).
- **B) LFU cache path**: loads once, then repeatedly calls `TaskService.getTask()` which populates/uses the LFU cache.

Run:

```bash
java -cp out tasktracker.bench.CachePerfBenchmark 5000 2000 200
```

Example result from a real run:

```text
A) JSON load per lookup: 15373.49 ms
B) LFU cache getTask(): 13.29 ms
Speedup: 1156.65x
```

Notes:
- The benchmark writes to `data/bench_tasks.ndjson`
- The baseline is intentionally pessimistic to show why caching helps.