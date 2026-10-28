# Checkpoint microbenchmark

Sample run on 2026-10-27 using Zulu Java 25.0.3 on macOS ARM64.
One employee, 2,000 warmup iterations and 10,000 measured iterations per scenario.
Undo/redo dispatch uses storage that performs no disk I/O and disables command logging.

| History steps | Shared capture (ns/op) | Copying baseline (ns/op) | Undo + redo (ns/pair) |
| --- | ---: | ---: | ---: |
| 10 | 199 | 395 | 8,133 |
| 1,000 | 180 | 2,102 | 3,201 |
| 10,000 | 69 | 12,875 | 2,828 |

Run `./gradlew -I tests/performance/history-performance.gradle checkpointPerformance` to reproduce the scenarios.
This sample was compiled and run directly with the same Java sources and cached runtime dependencies.
The baseline emulates the former working-list and history-index copies; it does not launch an older revision.
Shared capture avoids work proportional to history size. Timings are diagnostic and affected by JIT compilation,
allocation and garbage collection; decreasing times across rows are not a claim that longer history is faster.
This is not an end-to-end UI benchmark. A changed commit after capture still copies the history index.
