# Employee history and rollback test plan

## Baseline and scope

Recorded local `master` and this branch's starting commit: `1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`.
The independent `codex/104-safe-data-saving` branch must use that same starting commit. It was not present in the
local refs during this implementation, so its base is still to be verified by its owner. No safe-saving commits or
storage-write changes are included here. The current checkout is `branch-undo-redo`.

This branch tests in-memory rollback through the existing storage API. Unchanged disk contents after a failed
write are not guaranteed by this branch. After integration, repeat the tests below with atomic saving and update
the combined user-facing explanation in the User Guide, Developer Guide and README.

## Automated behavioral coverage

Run with Java 25 from the repository root:

```sh
./gradlew check coverage
```

| Acceptance behavior | Automated coverage |
| --- | --- |
| Logic commits once for each of the seven modifying commands; undo/redo traverse mixed sequences and both boundaries without committing | `HistoryCommandTest.execute_allModifyingCommands_commitOnceAndTraverseEveryState` |
| One history-action declaration enables committing and rollback; failed centralized commits restore data, view and the entire redo tail before saving | `HistoryTransactionTest.execute_declaredModification_commitsWithoutCommandBookkeeping`; `HistoryTransactionTest.execute_commitFailure_restoresWorkingDataViewAndDiscardedRedoTail`; per-command save-failure tests |
| Exact success/boundary output; surrounding spaces/tabs; arguments, aliases and capitalization; syntax checked before availability | `AddressBookParserTest` history tests; `HistoryCommandTest`; `HistoryTransactionTest.execute_historySyntaxCheckedBeforeAvailability` |
| Entire multi-row import is one entry; redo after the source changes and disappears | `HistoryCommandTest.execute_allModifyingCommands_commitOnceAndTraverseEveryState` |
| Archive status, hidden employees and stored order across clear/archive/unarchive/delete | `HistoryCommandTest`; `ModelHistoryTest.undoAndRedo_archivedView_preservesPredicateAndObservableLists` |
| Actual changes discard redo; identical edits, empty clears, read-only and invalid commands preserve redo | `ModelHistoryTest.commit_identicalDataPreservesRedoButRawIdOrOrderChangesDiscardIt`; `HistoryTransactionTest` no-op/read-only/invalid-command tests |
| No-op edit/empty clear after undo resets view, then failed save restores the original archived predicate and complete redo tail | `HistoryTransactionTest.execute_noOpAfterUndoThenSaveFailure_restoresOriginalViewAndRedo` |
| One representative save failure for all nine data-changing commands | `HistoryTransactionTest.execute_eachDataChangingCommand_saveFailureRestoresAllState` |
| Generic permission and I/O messages preserve `getMessage()` and cause; original runtime exception from saving or serialization is rethrown | `LogicManagerTest`; `HistoryTransactionTest` permission/I/O/runtime and serialization tests |
| Failure after some import additions and failure after tentative commit restore the full history, not just records/pointer | `HistoryTransactionTest` partial-import and tentative-commit tests |
| Delete archived employee, reuse ID with different case, traverse both directions and compare raw strings/order | `HistoryCommandTest.execute_deleteArchivedThenReuseCaseVariant_preservesRawValuesAndOrder` |
| Zero-match search reveals the matching employee on undo without replacing its predicate | `ModelHistoryTest.undo_zeroMatchSearchRevealsRestoredEmployeeWithoutReplacingPredicate` |
| Archived checkpoint restoration applies the predicate directly; stored and filtered observable-list identities survive | `ModelHistoryTest.checkpoint_laterChangesDoNotAffectWorkingDataOrEntireHistory`; per-command rollback tests |
| `setAddressBook()` is uncommitted, preserves view, and neither commits nor resets history | `ModelHistoryTest.setAddressBook_replacesOnlyWorkingData` |
| Stable checkpoints after subsequent changes and redo truncation; repeated restoration; immutable containers and shared snapshots | `ModelHistoryTest` checkpoint tests |
| Save/reload after undo and redo yields the correct current data and one initial snapshot with no available history | `HistoryTransactionTest.execute_undoAndRedoSaveReload_startsNewSessionWithoutHistory` |
| Read-only commands still save and keep view changes on save failure; export/backup files survive undo and save failure | `HistoryTransactionTest` read-only tests |
| Ordinary address book/model equality excludes history | `ModelHistoryTest.equals_differentHistoriesPreservesExistingEquality` and existing equality tests |
| Existing JSON validation, legacy missing `archived`, ID uniqueness, seven-column CSV export and legacy six-column import | Existing storage, employee, import/export and parser suites (unchanged storage implementation) |
| Help summary lists all 15 supported command words, including import/export/backup and archive/unarchive, with file and employee-ID formats | `HelpCommandTest.commandSummary_listsEveryCommand`, `commandSummary_showsFileCommandFormats` and `commandSummary_showsEmployeeIdFormats` |

`EmployeeDataAssertions` compares all seven stored values in order, including raw IDs, independently of employee
or model equality. Predicate references, observable-list identities and every restored history state are asserted
separately. Model stubs implement the new API; existing save-failure expectations now require rollback.
History command sequences run through `LogicManager`, which owns commits as well as checkpointing and saving.
Commands declare `COMMIT`, `NAVIGATE` or the default `NONE`; they do not maintain a separate rollback flag or call
`commitAddressBook()` themselves. This replaces the initial per-command bookkeeping in response to PR #115 review.

## UI and performance acceptance

The standalone harness in `tests/performance/seedu/address/ui/HistoryPerformance.java` creates the actual
`MainWindow`, `CommandBox`, result display and employee panel with isolated storage under `build/history-performance`.
It never reads or writes the user's configured employee file or preferences.

Run on a desktop with Java 25, at least two CPU cores and 8 GB RAM, a local SSD and no other resource-intensive
applications running, as documented in the Developer Guide:

```sh
./gradlew -I tests/performance/history-performance.gradle historyPerformance
```

The runner is opt-in and outside ordinary JUnit/CI. Keep the window visible and do not interact with it during the
run. It creates a deterministic 1,000-employee fixture with distinct IDs and 100-character name, phone, email,
department and role values. Each of three sessions starts from that same fixture with no history, makes 100 genuine
edits to the first employee's role while keeping exactly 1,000 employees, then executes 100 undos and 100 redos.
It records all 600 history-operation durations; setup edits are not included in those timings.

Timing starts immediately before dispatching the command box's action event. The command parses, checkpoints,
executes, serializes and saves through real production code. Timing ends in the first asynchronous scene-snapshot
callback after the command. JavaFX renders that snapshot at the next frame, including CSS/layout, before invoking
the callback ([JavaFX Scene snapshot contract](https://openjfx.io/javadoc/17/javafx.graphics/javafx/scene/Scene.html#snapshot(javafx.util.Callback,javafx.scene.image.WritableImage))).
This measures rendering completion, not physical display scanout; the snapshot adds conservative rendering overhead.
The runner verifies the completed result text, visible updated employee card, exactly 1,000 stored/visible employees,
unchanged UI list binding and cleared/default-styled command box. Disk read-back is checked after stopping the timer.

Before each session, UI smoke checks submit `undo`, `redo`, `undo 2`, `redo now` and `UNDO` at the initial boundary.
They verify retained input, the existing error style and unchanged data/history. JUnit asserts the exact messages.

The two-second threshold applies to every timed history operation and is enforced only by this manual runner.
Output includes `timings.csv`, `summary.txt`, isolated JSON files and `completed-frame.png` under
`build/history-performance`. Copy the timing CSV to `tests/performance/results` when recording an acceptance run.

### Recorded run: 21 October 2026, before atomic-saving integration

- Result: **PASS**, 600/600 timed operations below 2,000 ms; maximum **29.423 ms**.
- Java: Homebrew OpenJDK **25.0.1**, ARM64; JavaFX **17.0.7**, matching ARM native libraries.
- OS/hardware: macOS **15.8**, Apple M4 MacBook Pro, **10 CPU cores**, **32 GB RAM**, internal **APPLE SSD AP1024Z**.
- UI: default **740 × 600** window; actual production controls and employee-card rendering; window visible.
- Fixture: same deterministic 1,000 valid employees in all three sessions; 100 genuine edits per session.
- Storage: existing JSON storage API on the local SSD, including automatic save on every operation.
- No additional build or test workload ran concurrently with the measured sessions.
- Raw timings: [2026-10-21-history.csv](performance/results/2026-10-21-history.csv).
- **Required after integration:** repeat all three sessions with atomic saving, record a separate maximum and environment,
  verify the safe-saving branch's base commit, and update the combined storage guarantee in user-facing documentation.

### Local Apple Silicon validation setup

The existing build declares Intel macOS JavaFX artifacts. The first unadjusted local suite failed in the existing
`AppUtilTest` with an incompatible native-library architecture. No product dependency changes were made. For this
run, the matching 17.0.7 `mac-aarch64` native libraries already available in the local Gradle cache were extracted
into `build/javafx-arm64`. Both the complete suite and the UI runner used:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
JAVA_TOOL_OPTIONS="-Djava.library.path=$PWD/build/javafx-arm64 --enable-native-access=ALL-UNNAMED" \
./gradlew check coverage

JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
JAVA_TOOL_OPTIONS="-Djava.library.path=$PWD/build/javafx-arm64 --enable-native-access=ALL-UNNAMED" \
./gradlew -I tests/performance/history-performance.gradle historyPerformance
```

Use the matching JavaFX native libraries for the test machine; the directory above is a local validation setup,
not a required production path. The standalone harness does not change the shipped classpath or build settings.

## Documentation gate and recorded validation

```sh
npm run build --prefix docs
```

Run explicitly after editing the guides, documentation README and diagrams. The generated `docs/_site` output is
not committed. Inspect the history transaction/model diagrams for render errors and check links to removed diagrams.
Release-wide version labels stay unchanged.

Recorded validation after the PR #115 review changes on 21 October 2026:

- Java 25 `./gradlew check coverage`: **PASS**, **446 tests**, zero failures/errors/skips; both Checkstyle tasks pass.
- Explicit MarkBind build: **PASS**, 12 pages. All nine referenced diagrams generated nonempty PNGs; the updated
  transaction and edit sequence diagrams were visually inspected. One pre-existing warning remains for the missing
  `/team/sjyjoshua.html` link in `docs/AboutUs.md`.
- `git diff --check`: **PASS**.

The local default Java diagram subprocess initially aborted and Graphviz was unavailable. To validate rendering
without changing project dependencies or diagram source settings, the successful documentation build used a local,
ignored wrapper at `build/docs-java/java`:

```sh
#!/bin/sh
exec /opt/homebrew/opt/openjdk@25/bin/java -Djava.awt.headless=true "$@" -Playout=smetana
```

Then, from the repository root:

```sh
PATH="$PWD/build/docs-java:$PATH" npm run build --prefix docs
```

Smetana is PlantUML's bundled Java layout engine. The normal documentation build remains `npm run build --prefix docs`
on a machine with Java and Graphviz configured. These local validation environment adjustments do not change
product behavior. The recorded performance timings above predate the review refactor; the integration acceptance
run remains required after atomic saving is combined with this branch.
