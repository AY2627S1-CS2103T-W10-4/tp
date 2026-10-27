# Employee history and atomic saving test plan — #104

## Similar-name performance validation — #199

`AddCommandPerformanceTest` runs actual adds against `ModelManager` with 10,000 employees for no-match, all-match
and sparse-match rosters, including archived records, Unicode names and repeated spaces. It checks total counts,
the five-ID preview, matches at the end of the roster and preservation of existing records. The exactly-five
boundary is also tested. Fixture setup is excluded from its generous 10-second severe-regression guard.

Run `./gradlew -I tests/performance/history-performance.gradle addPerformance` for the opt-in benchmark comparing
cached and uncached normalisation scans and measuring actual add commands at 100, 1,000 and 10,000 employees.
See `tests/performance/results/2026-10-27-add.md` for measured results and limitations. Full scans remain O(N);
the benchmark excludes disk saving, history commits and UI rendering and has no CI speedup threshold.

## Baseline and integration scope

Both feature branches start at recorded local `master` `1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`.
This merges `master` at `b67b54b538d117d8d3a2f7de0da667f8be2f9692` (undo/redo PR #115) into atomic-saving
commit `430fb87b` on `branch-safe-data-saving` (PR #116). Both independent validation records are retained below.
The combined behavior preserves stored bytes during handled save failures and restores records, complete history
and the effective view for data-changing commands. Public storage APIs, JSON schema, CSV writing, preferences,
validation and existing command success messages remain unchanged.

## Atomic storage and presentation coverage

Run from the repository root with Java 25:

```sh
./gradlew check coverage
```

| Acceptance behavior | Tests |
| --- | --- |
| Real new file and real existing-target atomic replacement; complete bytes and no leftover temporary file | `AtomicFileWriterTest.write_newAndExistingFile_replacesCompleteContents` (never gated on atomic-move availability) |
| Creation, temporary protection, open, partial write, force, close, final protection, replacement, unsupported atomic move and access-denied faults | `AtomicFileWriterTest.write_eachPreReplacementFault_preservesExistingOrAbsentDestination`; each fault repeats with an existing and absent destination |
| Short writes consume the entire buffer; force and close precede replacement | `AtomicFileWriterTest.write_shortWrites_consumesEntireBufferAndClosesBeforeReplacement` |
| Partial-write failure never alters the destination; force/close failures never reach replacement | The per-fault matrix verifies exact original bytes/absence and zero replacement calls |
| Cleanup failure is suppressed on the original failure; write plus close failure also retains the original | `AtomicFileWriterTest` cleanup and write/close tests; cleanup failure may leave an orphan but never changes the destination |
| No cleanup after successful replacement | `AtomicFileWriterTest.write_success_doesNotAttemptCleanupAfterCommit` |
| Restricted access before the first employee byte; new destination retains protection | `AtomicFileWriterTest.write_newFile_restrictsAccessBeforeFirstByteAndRetainsProtection`; the short-write handle checks actual native protection before its first write |
| Existing POSIX permissions, owner and group retained; owner-write required even if another class can write | Real POSIX tests in `AtomicFileWriterTest` |
| POSIX ownership/group mismatch, failed restrictive permissions and failed final permission/owner verification | `AtomicFileWriterProtectionTest` POSIX tests; assert no ownership setter is called |
| Real Windows ordered access ACL and owner retained; DOS read-only rejected without clearing it | Real Windows tests in `AtomicFileWriterTest` |
| Windows policy exercised on every platform: owner-only full-access ACL before bytes, opaque ordered entries and flags, same-owner requirement, failed ACL round-trips, missing views, DOS protection, access-denied classification | Portable attribute-view doubles in `AtomicFileWriterProtectionTest`; they do not substitute for native Windows replacement tests |
| Unsupported views and unwritable destination fail before temporary creation/replacement | `AtomicFileWriterTest` missing-view and unwritable tests |
| Bare relative filename, symlink parent with missing subdirectory, valid leaf link, dangling leaf link and directory destination | `AtomicFileWriterTest` path tests; real temporary parent equals the resolved parent |
| Unsupported atomic movement has the exact required IOException message and original cause, with no destructive fallback | Per-fault matrix asserts one replacement attempt, unchanged bytes/absence and original `AtomicMoveNotSupportedException` as cause |
| JSON conversion and serialization finish before invoking the writer; serialization failure cannot touch the destination | `JsonAddressBookStorageTest` writer payload, conversion failure and midway serialization failure tests (existing and absent file) |
| Complete pretty-printed UTF-8 payload, exact ordered raw values, archive status and only the existing schema fields | `JsonAddressBookStorageTest` payload and real creation/replacement tests |
| Writer exception identity, classification and causes propagate unchanged through JSON storage | `JsonAddressBookStorageTest.saveAddressBook_writerFailures_preserveExceptionClassificationAndCauses` |
| Legacy missing archive status, empty arrays, invalid employees, missing/null arrays and ignored unknown properties | Existing JSON tests plus `readAddressBook_unknownPropertiesIgnoredAndOrphanTemporaryNotLoaded` |
| Orphan temporary files are not loaded | The unknown-property/orphan test leaves the configured file absent and verifies an empty load result |
| Storage manager, preferences and existing backup behavior remain compatible | Existing `StorageManagerTest`, `JsonUserPrefsStorageTest` and `BackupCommandTest` |
| Exact automatic-save generic, unsupported-atomic, protection and permission messages, retaining exception causes | `AtomicSaveLogicTest.execute_automaticSaveFailures_keepExactMessagesAndCausesWithMemoryRollback`; permission input includes paths and additional diagnostic text |
| Real read-only save failure preserves disk and restores memory | `AtomicSaveLogicTest.execute_realReadOnlySaveFailure_preservesFileAndMemory` |
| Real atomic-save rejection after undo preserves disk bytes, effective predicate, records and the entire history for new changes, undo and redo | `AtomicSaveLogicTest.execute_changeUndoAndRedoWithReadOnlyFile_preservesDiskViewAndEntireHistory`; traverses every restored state and reloads the last successful file without history |
| Failed identical edit and empty clear after undo restore the prior view and complete redo sequence | `AtomicSaveLogicTest.execute_noOpAfterUndoWithReadOnlyFile_preservesDiskViewAndRedoSequence`; uses the real writer and read-only destination |
| Every backup IOException keeps its distinct presentation; successful naming/location and output remain unchanged | `BackupCommandTest`, including its failure matrix and real successful backups |
| Successful backup followed by failed automatic save retains readable backup and presents the automatic-save error | `AtomicSaveLogicTest.execute_backupThenAutomaticSaveFails_keepsBackupAndPresentsAutomaticSaveError` |
| Exact clear/missing-ID/header-only-import output alongside successful `undo`; successful read-only/no-op commands still save | Remaining `AtomicSaveLogicTest` tests; path rendering follows the existing platform behavior |

The filesystem seam stays inside `AtomicFileWriter`; production delegates to ordinary JDK operations.
Package-private injection points in JSON storage, logic and backup construction isolate tests without changing
public APIs. Low-level faults are not repeated across every employee command. The serialization failure test
installs a scoped failing Jackson serializer and restores the original mapper in `finally`; production JSON
utilities are unchanged. JUnit uses the repository's existing sequential configuration.

## History and rollback coverage

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
| Existing JSON validation, legacy missing `archived`, ID uniqueness, seven-column CSV export and legacy six-column import | Existing storage, employee, import/export and parser suites (existing formats) |
| Help summary lists all 15 supported command words, including import/export/backup and archive/unarchive, with file and employee-ID formats | `HelpCommandTest.commandSummary_listsEveryCommand`, `commandSummary_showsFileCommandFormats` and `commandSummary_showsEmployeeIdFormats` |

`EmployeeDataAssertions` compares all seven stored values in order, including raw IDs, independently of employee
or model equality. Predicate references, observable-list identities and every restored history state are asserted
separately. Model stubs implement the new API; existing save-failure expectations now require rollback.
History command sequences run through `LogicManager`, which owns commits as well as checkpointing and saving.
Commands declare `COMMIT`, `NAVIGATE` or the default `NONE`; they do not maintain a separate rollback flag or call
`commitAddressBook()` themselves. This replaces the initial per-command bookkeeping in response to PR #115 review.

## Platform gate

`.github/workflows/gradle.yml` already runs `./gradlew check coverage` with Java 25 on `ubuntu-latest`,
`macos-latest` and `windows-latest`. Keep real new-file and existing-file replacement tests unconditional. An
unsupported atomic replacement must fail that supported-platform gate; it is not a reason to skip those tests.
Only capability-specific tests use explicit assumptions: native POSIX or Windows attribute views and permission
to create symbolic links. Portable Windows policy tests run on all platforms.

Local macOS validation does not count as Linux or Windows evidence. All three platforms must pass on the merged revision before merging PR #116. The initial [PR #116 workflow run](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/runs/39570758915)
passed on Linux, failed on Windows and cancelled macOS through matrix fail-fast. The corrected standalone commit `430fb87b` then
[passed on all three platforms](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/runs/39572645251).
Repeat these checks on the merged revision.

### Windows line-ending regression

The JSON payload test initially hard-coded LF line endings in its pretty-print prefix assertion. Jackson's
existing pretty printer uses the platform line separator, which is CRLF on Windows. Use `System.lineSeparator()`
in the assertion to preserve the production format and check the same structure on every platform.

Local reproduction with the test JVM's `line.separator` set to CRLF failed at that assertion before the fix.
After the fix, all 20 `JsonAddressBookStorageTest` tests pass with CRLF, and the normal Java 25
`./gradlew check coverage` run passes with the same 454-test total and two Windows-only skips described below.
The CRLF run checks formatting portability; native Windows replacement and ACL checks still require Windows CI.

## Standalone atomic-saving validation — 21 October 2026

- Java 25 `./gradlew check coverage`: **PASS**, **454 tests**, zero failures/errors; **2 Windows-only native
  attribute tests skipped** with explicit assumptions on macOS. The portable Windows ACL tests passed.
- Runtime: Homebrew OpenJDK **25.0.1**, macOS **15.8**, Apple Silicon. Real new-file creation, existing-file
  replacement, POSIX protections and all symbolic-link cases passed on the local filesystem.
- Baseline `LogicManagerTest` save-failure expectations are unchanged and pass. No employee history is present.
- Checkstyle production and test tasks: **PASS**. `git diff --check`: **PASS**.
- Documentation: `npm ci` followed by `npm run build` in `docs`: **PASS**, **12 pages**; all **20** referenced
  PlantUML diagrams generated nonempty PNGs. The pre-existing `/team/sjyjoshua.html` link warning remains in
  `docs/AboutUs.md`.
- Documentation toolchain: locked MarkBind CLI **5.1.0**, Node **24.16.0**, npm **11.13.0**, Homebrew Java **25.0.1**,
  Graphviz **16.1.0**. Java's headless AWT setting was used; diagrams used Graphviz, not a substitute layout engine.
- Deployment declares `MarkBind/markbind-action@v2` and MarkBind **^7.2.1**, unlike the repository's dependency
  **^5.1.0**. No dependency or deployment upgrades were made. The undo/redo owner must record the resolved
  deployment version and verify the combined documentation with that toolchain too.

The repository's macOS JavaFX dependencies target Intel. Local Apple Silicon validation used the matching
JavaFX **17.0.7** ARM native libraries already available under ignored `build/javafx-arm64`:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
JAVA_TOOL_OPTIONS="-Djava.library.path=$PWD/build/javafx-arm64 --enable-native-access=ALL-UNNAMED" \
./gradlew check coverage
```

Documentation was built from `docs` after `npm ci` with Java and Graphviz on the path:

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
PATH="/opt/homebrew/opt/openjdk@25/bin:/opt/homebrew/bin:$PATH" \
JAVA_TOOL_OPTIONS=-Djava.awt.headless=true npm run build
```

These are local validation environment settings, not product dependencies or requirements on installation paths.
This record describes the independent atomic-saving branch before integration. Its corrected commit later passed
all three platform jobs as linked above; that evidence does not replace CI on the merged revision.

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

## Standalone history documentation gate and recorded validation

```sh
cd docs
npm ci
npm run build
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

## Combined release gate — after integrating both independently passing branches

The integration owner and undo/redo branch owner must complete and record all of the following before release:

1. Update both PRs against current `master`; both must reference **#104**. Request **@sjyjoshua** as reviewer and
   require green CI and approval before merging.
2. Run the full Java 25 check and coverage suite again, including real replacement on Linux, macOS and Windows.
3. Verify failed mutations, undo and redo preserve the last file bytes, working records, effective predicate and
   **complete history**, rather than only restoring the history pointer.
4. After undo, verify failed no-ops restore the previous view and preserve redo. Verify a failed new change leaves
   the original redo sequence intact.
5. Restart from the last successful employee file. Verify exact ordered employee values and archive state, one
   initial snapshot, and no available undo or redo.
6. Repeat the history branch's specified **three-session** rendered-UI performance run with atomic saving enabled:
   the same 1,000-employee fixture, 100 genuine edits, 100 undos and 100 redos per session; all 600 timed history
   operations must stay below 2,000 ms. Follow that branch's documented timing boundary and hardware/environment
   conditions, record raw timings and the maximum, and verify persisted data outside each measured interval.
7. The undo/redo branch owner must reconcile the User Guide, Developer Guide, documentation README and combined
   test plan to explain the final memory/disk commit boundary. Run `npm ci` then `npm run build` explicitly with
   Java and Graphviz, inspect generated diagrams, and additionally build with the deployment toolchain. Record
   both resolved MarkBind versions. The docs workflow currently runs only on pushes to `master`.
8. Close **#104** only after both branches and all combined acceptance checks are complete. Record completed
   integration checks separately below; local validation does not replace platform CI or reviewer approval.

## Combined validation — 21 October 2026

- Resolved the merge of `master` (`b67b54b5`, including PR #115) into `branch-safe-data-saving` (`430fb87b`).
  The production logic retains master's centralized commits and checkpoint rollback; employee storage retains
  the atomic writer. Standalone assertions about unknown `undo` and retained failed mutations are replaced with
  the integrated behavior. The User Guide, Developer Guide, READMEs and transaction/storage diagrams agree.
- Java 25 `./gradlew check coverage`: **PASS**, **486 tests**, zero failures/errors, **2 Windows-only native
  attribute tests skipped** on macOS; both Checkstyle tasks pass. This includes all existing writer faults,
  serialization and backup tests, history tests, and new real read-only failure integration cases for changes,
  undo, redo and no-ops after undo. Reload checks verify the last successful file and no initial undo/redo.
- Explicit `npm ci` followed by `npm run build` in `docs`: **PASS**, **12 pages**, all **9** referenced diagrams
  rendered to nonempty PNGs. The updated transaction and storage diagrams were visually inspected. The existing
  `/team/sjyjoshua.html` link warning remains. Versions: MarkBind **5.1.0**, Node **24.16.0**, npm **11.13.0**,
  Homebrew OpenJDK **25.0.1**, Graphviz **16.1.0**, with headless AWT and Graphviz rendering.
- Three-session UI acceptance with real atomic saving: **PASS**, **600/600** timed operations below **2,000 ms**;
  maximum **36.052 ms**. Each session used the same 1,000-employee fixture, 100 genuine edits, 100 undos and
  100 redos. UI smoke checks, rendered-frame assertions and disk read-back checks passed. Raw timings:
  [2026-10-21-atomic-history.csv](performance/results/2026-10-21-atomic-history.csv).
- Performance environment: Homebrew OpenJDK **25.0.1**, JavaFX **17.0.7** with the local ARM native library path,
  macOS **15.8**, Apple M4 MacBook Pro, **10 CPU cores**, **32 GB RAM**, internal **APPLE SSD AP1024Z**;
  visible **740 × 600** window. JavaFX logged its existing Intel-resource loading warnings before using the local
  native libraries. No other build or test workload ran concurrently with the measured sessions.
- Remaining release gates: Linux/macOS/Windows CI on the merged revision, verification with the deployment
  MarkBind **^7.2.1** toolchain (including its resolved version), and reviewer approval for PR #116. These are
  not claimed passes; #104 remains open until all combined acceptance checks and both PRs are complete.
