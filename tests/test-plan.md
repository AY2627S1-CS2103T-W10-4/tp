# Atomic employee-file saving test plan — #104

## Baseline and standalone scope

`branch-safe-data-saving` starts at recorded local `master`
`1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`. This is also the merge base of `branch-undo-redo`; no history-branch
commits are incorporated. Public storage APIs, command syntax, validation, successful output, employee JSON schema,
CSV writing and preference storage remain unchanged. No in-memory rollback or new command is implemented here.
The baseline `LogicManagerTest` still expects a mutation to remain in memory when its automatic save fails.

## Automated coverage

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
| Exact automatic-save generic, unsupported-atomic, protection and permission messages, retaining exception causes | `AtomicSaveLogicTest.execute_automaticSaveFailures_keepExactMessagesAndCausesWithoutMemoryRollback`; permission input includes paths and additional diagnostic text |
| Real read-only save failure preserves disk but leaves the completed memory mutation | `AtomicSaveLogicTest.execute_realReadOnlySaveFailure_preservesFileButLeavesMemoryMutation` |
| Every backup IOException keeps its distinct presentation; successful naming/location and output remain unchanged | `BackupCommandTest`, including its failure matrix and real successful backups |
| Successful backup followed by failed automatic save retains readable backup and presents the automatic-save error | `AtomicSaveLogicTest.execute_backupThenAutomaticSaveFails_keepsBackupAndPresentsAutomaticSaveError` |
| Exact clear/missing-ID/header-only-import output and unknown `undo`; successful read-only/no-op commands still save | Remaining `AtomicSaveLogicTest` tests; path rendering follows the existing platform behavior |

The filesystem seam stays inside `AtomicFileWriter`; production delegates to ordinary JDK operations.
Package-private injection points in JSON storage, logic and backup construction isolate tests without changing
public APIs. Low-level faults are not repeated across every employee command. The serialization failure test
installs a scoped failing Jackson serializer and restores the original mapper in `finally`; production JSON
utilities are unchanged. JUnit uses the repository's existing sequential configuration.

## Platform gate

`.github/workflows/gradle.yml` already runs `./gradlew check coverage` with Java 25 on `ubuntu-latest`,
`macos-latest` and `windows-latest`. Keep real new-file and existing-file replacement tests unconditional. An
unsupported atomic replacement must fail that supported-platform gate; it is not a reason to skip those tests.
Only capability-specific tests use explicit assumptions: native POSIX or Windows attribute views and permission
to create symbolic links. Portable Windows policy tests run on all platforms.

Local macOS validation does not count as Linux or Windows evidence. Both platform runs remain required before
merge; no Linux/Windows run has been performed from this local checkout.

## Recorded validation — 21 October 2026

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
There are no implementation deviations from the approved specification. Linux/Windows CI and the combined
release checks below are pending gates, not claimed passes.

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
8. Close **#104** only after both branches and all combined acceptance checks are complete. This standalone
   implementation does not close the issue or claim the combined rollback/performance checks have passed.
