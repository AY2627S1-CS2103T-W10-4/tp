# Delete employee test plan

This plan covers the approved `delete id/EMPLOYEE_ID` feature. Run commands in HuntR's command box. Reset the fixture before each independent case unless the case specifies a sequence.

## Scope and fixture

The command permanently deletes exactly one employee from the complete roster. There is no confirmation, undo, batch deletion, history, or backup. Use these records for manual checks:

| ID | Name | Phone | Email | Department | Role |
| --- | --- | --- | --- | --- | --- |
| E0123 | John Tan | 91234567 | johntan@example.com | Engineering | Software Engineer |
| E0456 | Betsy Crowe | 92345678 | betsy@example.com | Finance | Accountant |
| E0789 | John Tan | 93456789 | john.other@example.com | Sales | Account Manager |

Create the first record with:

```text
add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer
```

Use the same field prefixes to add the other two rows. IDs match `E[0-9]{4}` exactly. `E0000` and `E9999` are valid, including their leading zeroes. Non-ASCII digits are invalid.

**Specification correction:** the existing index-based `edit` command does not accept ID edits. Preserve that behavior; ordinary edits retain the existing valid ID. Strict ID validation applies to additions, model construction, parsing employee IDs, and JSON loading. Do not add ID editing.

## Exact output contract

The common usage block is:

```text
delete: Deletes one employee by employee ID from all stored employee records.
Parameters: id/EMPLOYEE_ID (uppercase E followed by exactly four digits)
Example: delete id/E0123
```

Append that block after exactly one newline for each parsing error below. Do not append a trailing newline.

| Code | Exact message before usage |
| --- | --- |
| FORMAT | `Invalid command format!` |
| ID | `Employee ID must be an uppercase E followed by exactly four digits (e.g. E0123).` |
| DUPLICATE | `Multiple values specified for the following single-valued field(s): id/` |
| EXTRA | `Unexpected arguments. Specify exactly one employee ID.` |

Successful deletion of the first fixture record must output:

```text
Deleted employee: ID: E0123; Name: John Tan; Phone: 91234567; Email: johntan@example.com; Department: Engineering; Role: Software Engineer
```

Other exact outputs (without usage):

```text
Unknown command.
No employee with ID E9999 was found.
Could not delete employee E0123: insufficient permission to save employee records. No employee records were changed.
Could not delete employee E0123: employee records could not be saved. No employee records were changed.
```

For another target, substitute only the employee ID and stored employee details.

## Functional acceptance cases

| Case | Input / sequence | Expected result |
| --- | --- | --- |
| D01 | `delete id/E0123` | Exactly E0123 removed; E0789 with the same name remains; exact full-details success |
| D02 | `delete    id/ E0123`, surrounding spaces, or tabs between tokens/after `id/` | Same result as D01 |
| D03 | `find Betsy`, then `delete id/E0123` | Hidden employee deleted; Betsy results and active predicate retained; `list` confirms removal |
| D04 | Search for a nonexistent name, then delete E0123 | Deletion succeeds while the visible list remains empty |
| D05 | `find John`, then delete E0123 | E0789 remains visible; existing filter and remaining roster order retained |
| D06 | Delete E0123 twice | First succeeds; second reports `No employee with ID E0123 was found.` |
| D07 | `delete id/E9999` when absent | Exact not-found message; no model or storage mutation |
| D08 | Delete from an empty roster | Same not-found behavior |
| D09 | Delete the final employee, exit, restart | JSON contains an empty `employees` array; no sample employees reappear |
| D10 | Delete E0123, then add a new employee using E0123 | Add succeeds and survives reload; no reserved-ID history |
| D11 | After success | Command input clears; result logged; no dialog, extra count, or empty-state message |
| D12 | After rejection or save failure | Input retained with existing error styling; roster, filter, and order unchanged |
| D13 | `edit 1 n/Updated Name` | Existing index-based edit works and retains ID |
| D14 | `edit 1 id/E9999` | Existing edit format error; no ID editing added |

All accepted deletion commands require a successful save before the live roster changes or success is returned.

## Validation cases

| Inputs | Expected error |
| --- | --- |
| `delete`, `delete id/`, whitespace-only ID | FORMAT |
| `delete 1`, `delete E0123`, `delete ID/E0123`, `delete extra id/E0123` | FORMAT |
| `delete id/e0123`, `delete id/EMP-0042`, `delete id/123` | ID |
| `delete id/E123`, `delete id/E01234`, `delete id/E 0123`, ID with an internal tab | ID |
| `delete id/E０１２３`, `delete id/E١٢٣٤`, `delete id/*` | ID |
| `delete id/E0123,E0456` | ID |
| `delete id/E0123 E0456`, `delete id/E0123 extra` | EXTRA |
| `delete id/E0123 n/John`, `delete id/E0123 x/value` | EXTRA |
| `delete id/E0123 id/E0123`, `delete id/E0123 id/E0456`, repeated prefix separated by tab | DUPLICATE |
| `delete extra id/bad id/E0123 n/John` | DUPLICATE takes precedence |
| `DELETE id/E0123` | `Unknown command.` without usage |

All rejected commands must leave storage untouched and must not call save. Check exact messages independently of production constants in representative tests.

## Persistence and failure injection

JUnit tests must exercise the real storage replacement path as well as the logic transaction. Inject failures deterministically at the storage seam; filesystem permissions alone are unreliable across operating systems.

For each injected failure, test both an existing destination and a previously missing destination:

1. Start with several employees and an active filter that hides the target.
2. Capture the live roster, observable-list reference, visible results, and original destination bytes (if any).
3. Inject a partial temporary-write failure, an access-denied failure, a replacement failure, or `AtomicMoveNotSupportedException`.
4. Execute deletion. Expect the exact permission/general save error, with no success result.
5. Verify unchanged live records/filter/order/list reference and byte-identical destination, or absence of a previously missing destination.
6. Verify failed staging files are removed during normal cleanup.
7. Remove the injected failure and retry the same command on the same logic instance. Verify success and reload the correct remaining records.

Additional checks:

* Inspect the live model inside the storage callback: the employee must still exist while the candidate is saved.
* A cleanup exception after replacement must be logged without reporting a failed save.
* No unsafe overwrite fallback is allowed if atomic replacement is unsupported.
* Add, edit, delete, and clear must all pass the save-before-publish and failure/retry checks below.
* Preserve all remaining fields and array order; do not add schema fields, timestamps, tombstones, or backups.

## Data compatibility

* Existing valid `E####` records load unchanged.
* Lowercase/custom IDs are rejected by `EmployeeId`, the add command, and JSON loading.
* A mixed file containing valid employees and one invalid ID is rejected in full and left unchanged during reading.
* Duplicate-ID fixtures use two identical valid uppercase IDs, so they continue testing uniqueness rather than format rejection.
* Startup on an invalid file follows the existing empty-roster-and-warning behavior. There is no automatic migration. Later successful record-changing commands may overwrite the invalid file with the running roster; `list`, `find`, `help`, and `exit` leave it untouched.

## Shared saving acceptance cases

This follow-up extends the deletion transaction to all commands that change employee records. Existing command syntax and messages are retained.

| Case | Input / setup | Expected result |
| --- | --- | --- |
| S01 | `add` a new valid employee with an active filter | Save before live changes; success shows all employees; reload matches the live roster |
| S02 | Filter to a non-first stored employee, then `edit 1 n/Updated Name` | Edit the first displayed employee; save before live changes; success shows all employees |
| S03 | `clear` with an active filter | Save an empty roster before clearing the live list; retain the filter |
| S04 | `clear` while already empty with no saved file | Persist an empty roster; reload stays empty |
| S05 | Edit a field to its existing value | Still save successfully and retain normal edit behavior |
| S06 | Each of add, edit, delete, and clear with injected partial-write, permission, replacement, or unsupported atomic-move failure | No live/filter/order/file changes; no success result; retry after removing the fault succeeds |
| S07 | Repeat S06 with no destination file | Failed command leaves the destination absent; retry creates the correct saved roster |
| S08 | `find`, `list`, `help`, and `exit` with storage that must never be called | Normal results, filtering, and help/exit flags; no save calls or file creation |
| S09 | S08 with an existing valid or invalid data file | Saved bytes remain unchanged |
| S10 | Duplicate add, invalid edit arguments, or an edit index outside the filtered list | Reject before saving; preserve live records and the active filter |

For S01–S03, inspect the live model inside the real storage callback both before and after writing: the old records and filter must remain visible until save returns. Preserve the live observable-list reference. For S06–S07, apply the full persistence-failure procedure above to every record-changing command, including byte comparison, temporary-file cleanup, and retry on the same logic instance. Existing non-delete save-error wording is retained; delete retains its approved specific messages. No non-atomic fallback is allowed.

## Automated coverage and commands

| Test class | Main coverage |
| --- | --- |
| `DeleteCommandParserTest` | Valid spacing, boundaries, exact usage/errors, duplicate precedence, invalid syntax |
| `AddressBookParserTest` | Full routing, tabs, legacy syntax, command capitalization |
| `DeleteCommandTest` | Global lookup, visible/hidden/empty searches, namesakes, missing/last employee, ID reuse |
| `DeleteCommandIntegrationTest` | Save-before-publish, filter/list preservation, restart data, rejection without save, add ID validation, edit compatibility |
| `LogicManagerIntegrationTest` | Shared save-before-publish, filtered edit targeting, add/edit filter resets, clear, no-op saves, read-only commands without storage, invalid-file preservation |
| `EmployeeIdTest`, `ParserUtilTest`, `EmployeeTest` | Shared strict ID domain and employee identity |
| `JsonAddressBookStorageTest` | Real replacement, fault injection and retry for all four record-changing commands, absent destination, cleanup, whole-file compatibility |
| Existing storage/model/add/edit tests | Regression coverage for shared components |

Run from the repository root with Java 25:

```sh
./gradlew check coverage
npm ci --prefix docs --ignore-scripts
npm run build --prefix docs
```

Review the generated JUnit, Checkstyle, coverage, and documentation build results. The feature-level gate is passing functional tests and existing automated checks, plus review of the rendered documentation.

## Manual release checks

* Verify command-box clearing/error styling and keyboard-only use in the real JavaFX UI.
* Run `help`: expect `Opened help window.` The window must show and copy `https://ay2627-cs2103t-w10-4.github.io/tp/UserGuide.html`. Verify the published destination; local source changes do not publish the website.
* On Windows, Linux, and macOS with Java 25, verify deletion and relaunch with both remaining records and an empty roster.
* Retain Developer Guide release requirements: at least 1,000 records; 20 delete trials restoring the dataset each time, each completing within 2 seconds including save, under its documented hardware conditions.
* Retain the documented display-resolution/scaling checks. These platform/performance checks are release gates, not new automated feature gates.

## Verification record — 2026-10-07

* Java 25: `./gradlew check coverage` passed, with 267 tests, zero failures/errors/skips, and both Checkstyle tasks passing.
* `./gradlew shadowJar` passed.
* `npm run build --prefix docs` generated all 12 pages. It reported the existing unrelated missing `team/sjyjoshua.html` link.
* The three changed PlantUML diagrams were rendered and inspected using PlantUML's built-in Smetana renderer. The normal site build still needs Graphviz for class diagrams; Graphviz is not installed on this machine. MarkBind's successful exit alone does not establish successful diagram rendering.
* The JAR launched in an isolated temporary folder using sample data. The available UI automation could not target the Java window, so input styling, clearing, Help copying, and other interactive checks are not marked passed. The test application was stopped.
* Cross-platform/performance checks and verification of the published Help URL remain release tasks.

### Shared saving follow-up — 2026-10-07

* Java 25: `./gradlew check coverage shadowJar` passed with 275 tests, zero failures/errors/skips, and zero Checkstyle violations.
* The storage tests cover four failure types for all four record-changing commands, each with an existing and an absent destination: 32 failure cases, each followed by a successful retry.
* A temporary command-line harness against the rebuilt JAR passed 28 scenarios, 333 assertions, and 165 command executions, including the deletion regression suite and a separate-process startup check. The shared-saving checks confirm that read-only commands work without saving and failed add/edit/delete/clear commands preserve live and saved records before a successful retry. This exercises the command pipeline, not the JavaFX interface.
* The documentation build generated all 12 pages. The pre-existing missing team-page link and missing Graphviz dependency remain; the build exit status does not establish complete diagram rendering.
* The previously reported Help URL failure and unverified interactive UI checks remain outside this saving follow-up.
