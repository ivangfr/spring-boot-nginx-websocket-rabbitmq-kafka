# Running Benchmarks

This project includes tools to benchmark and compare different Spring Boot optimization techniques. Five configurations are tested, ranging from a standard Uber JAR to JVM AOT Cache combined with Spring AOT, to measure the impact each technique has on startup time, time to ready, memory usage, and CPU time.

## Prerequisites

**1. Start the required Docker Compose services:**

```bash
podman compose up -d zookeeper kafka rabbitmq postgres
```

**2. Build the application JAR:**

```bash
./mvnw clean package --projects news-app
```

## Automated Benchmark

`benchmark.sh` runs all 5 configurations automatically for a given number of runs, prints live results to the console, and saves everything to a timestamped CSV file.

**Run with 3 iterations per configuration (default):**

```bash
./benchmark.sh 3
```

You can pass any number of runs as the argument. If omitted, it defaults to `3`.

**What happens during execution:**

- For each run, the script builds (or rebuilds) the necessary artifacts, starts the app, measures metrics, and shuts it down.
- Progress is printed to the console as each run completes, for example:
  ```
  [1/15] Run 1/3: Uber JAR
    --> RSS=245760 Time=4.23 Startup=2.341 TimeToReady=3.102
  ```

**Output:**

A CSV file is created in the project root named after the run timestamp:

```
results-YYYY-MM-DD-HHmmss.csv
```

The file contains one row per run, plus a computed averages block at the end:

```
Section,Run,RSS,Time,Startup,TimeToReady
Uber JAR,1,245760,4.23,2.341,3.102
...
Averages:
Uber JAR,avg,244800,4.18,2.310,3.089
...
```

## Single Configuration

To measure a single configuration manually, set the `JAVA_JAR_COMMAND` environment variable to the command that starts the app, then run `collect-metrics.sh`:

```bash
export JAVA_JAR_COMMAND="java -jar news-app/target/news-app-1.0.0.jar"
./collect-metrics.sh
```

The script starts the app, waits for `/actuator/health` to return HTTP 200, captures metrics, prints them to stdout, and then stops the process:

```
RSS,CPUTIME,STARTUP_TIME,TIME_TO_READY
245760,4.23,2.341,3.102
```

For the exact commands needed to prepare and run each of the 5 configurations individually, see [manual-steps.md](manual-steps.md).

## Metrics Collected

| Metric | Description |
|--------|-------------|
| `RSS` | Resident Set Size — physical memory used by the process (KB) |
| `CPUTIME` | Total CPU time consumed during startup (seconds) |
| `STARTUP_TIME` | Time reported in the Spring Boot "Started" log line (seconds) |
| `TIME_TO_READY` | Wall-clock time from process start until `/actuator/health` returns HTTP 200 (seconds) |

## Configurations Tested

| Configuration | Description |
|---------------|-------------|
| Uber JAR | Standard executable JAR |
| Extracted JAR | JAR extracted with `jarmode=tools` for faster class loading |
| CDS | Class Data Sharing — pre-archives loaded classes into a shared archive |
| AOT Cache | JVM Ahead-of-Time Cache — caches JIT-compiled code across restarts |
| AOT Cache + Spring AOT | JVM AOT Cache combined with Spring AOT processing at build time |

## Viewing Results

Open `benchmark-viewer.html` in a browser:

```bash
open benchmark-viewer.html
```

Then load the generated CSV file (e.g. `results-2025-01-01-120000.csv`) using the file picker in the viewer. Charts will display average RSS, CPU time, startup time, and time to ready across all configurations.

## Shutdown

To stop and remove the Docker Compose services started for benchmarking:

```bash
podman compose down -v
```
