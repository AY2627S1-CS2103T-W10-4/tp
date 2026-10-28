---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# HuntR Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

Employee-data command execution and saving are shown in the [transaction diagram](#checkpoints-and-transaction-boundary).

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `EmployeeListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

A few small classes keep the logic of the UI out of the JavaFX classes. `CommandBox` keeps a `CommandHistory` of the commands that were typed, for the <kbd>↑</kbd> and <kbd>↓</kbd> keys, and uses `CommandCompleter` to complete the command word for the <kbd>Tab</kbd> key. `EmployeeCard` uses `DepartmentColour` to give each department its colour and `Initials` to make the initials shown in the avatar. These four classes do not use JavaFX, so they are tested without a window.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Employee` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete an employee).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. For data-changing commands, Logic saves the employee state and returns the `CommandResult` only after saving succeeds. Read-only commands skip employee-file saving. Data-changing commands use the checkpoint and rollback boundary described below.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="700" alt="Model structure including employee manager IDs and reporting validation" />


The `Model` component,

* stores working data in a `VersionedAddressBook` and its `UniqueEmployeeList`, with immutable history snapshots and a current-state pointer.
* stores the `Employee` objects selected by the current filter, such as search results, in a separate _filtered_ list, and wraps it in a `SortedList` that puts the employees in the order of the current `sort`. Sorting only changes the comparator of this `SortedList`; the stored order does not change. It exposes the sorted list as an unmodifiable `ObservableList<Employee>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

#### Atomic employee-file saving

`JsonAddressBookStorage` retains its public API. It converts the complete model to `JsonSerializableAddressBook`,
serializes using `JsonUtil.toJsonString()` and encodes UTF-8 **before** invoking the storage-local
`AtomicFileWriter.write(Path, byte[])`. The generic JSON/file utilities, preferences and CSV writers are unchanged.
JSON backups already use this storage class and therefore share its atomic writer.

Backups use `saveNewAddressBook`, which refuses existing destination files or symbolic links.
After writing, forcing, closing and protecting the sibling temporary file, the writer publishes it using
`Files.createLink(destination, temporary)`. The final name refers to complete contents as soon as it exists;
there is no empty reservation, and an occupied name causes a collision without replacing it.
Successful link creation is the backup commit point. Removing the temporary name is best effort: cleanup failure
is logged but still reports backup success. Interruption can leave a `.tmp` name, but cannot expose an empty
backup under its final name. This does not promise survival of filesystem or hardware failures.
Backups require hard-link support; unsupported file systems fail without an unsafe copy or reservation fallback.
`BackupCommand` always uses a ten-digit, zero-padded counter, starting at `0000000000` for each timestamp and
retrying occupied names. New backup names therefore sort in creation order within a second, including beyond nine
backups. Older backups without counters remain untouched; clock changes and external file changes are outside
this ordering guarantee.

For ordinary employee-file saves, the writer resolves the destination and inspects its supported access attributes,
creates missing parent directories, then creates a uniquely named sibling `.huntr-*.tmp` file. It establishes and verifies restricted
temporary access before writing any employee bytes, handles short writes until the entire buffer is consumed,
forces the file and closes it. It then prepares and verifies final access protections and uses
`Files.move` with `ATOMIC_MOVE` and `REPLACE_EXISTING`. It never pre-creates, truncates or deletes the destination,
and never falls back to an ordinary overwrite or delete-then-move.

**Successful replacement is the commit point.** No fallible cleanup, attribute update or logging follows it.
A handled pre-replacement failure preserves the existing destination bytes, or leaves a previously absent
destination absent. Temporary-file cleanup is attempted on failure; cleanup exceptions are suppressed on the
original failure. Created parent directories may remain. This is a handled-save-failure guarantee, not a
crash-recovery journal or a promise of directory-entry durability after power loss.

The supported environment is a single-user application on local filesystems with atomic-replacement and required
attribute capabilities. The access-protection contract is deliberately limited:

| Concern | Policy |
| --- | --- |
| POSIX temporary/new file | Create with owner read/write only (`0600`) and verify before writing bytes. Retain the naturally assigned owner/group for a new destination. |
| Existing POSIX file | Preserve the nine POSIX permission bits. Its owner and group must match the sibling temporary file's naturally assigned owner/group; otherwise fail. |
| Windows temporary/new file | Before writing bytes, set and verify an owner-only access ACL granting full file access to the temporary file's creator-assigned owner principal. Retain this ACL and a clear DOS read-only flag for a new destination. |
| Existing Windows file | Require matching ownership. Copy the same-provider access ACL's ordered entries as an opaque list and verify the copied list and owner before replacement. Do not translate principals or reconstruct inheritance. |
| Read-only file | Reject an existing file that is not writable. Also require POSIX owner-write permission or, on Windows, an explicitly clear DOS read-only flag. Never clear destination protection. |
| Unsupported protection | Fail before replacement if required views, compatible ownership, restricted temporary access, permission preservation or ACL round-tripping cannot be established. Actual access-denied exceptions retain their classification. |

Java exposes [POSIX permissions](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/attribute/PosixFileAttributeView.html),
[access ACLs](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/attribute/AclFileAttributeView.html)
and [DOS flags](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/attribute/DosFileAttributeView.html)
through separate views. Windows support covers same-owner, same-provider access-ACL copying only, not arbitrary
security descriptors, audit ACLs or metadata unavailable through the Java view. POSIX special mode bits, extended
ACLs and arbitrary extended attributes are outside the preservation contract. There is no ownership/group migration,
principal remapping or cross-filesystem ACL migration, and no promise to preserve unrelated timestamps or metadata.

Path resolution is internal: configured paths exposed by storage remain unchanged. Bare filenames resolve against
the working directory. Symbolic-link parents are resolved before choosing the temporary directory. A valid leaf
link remains in place while its real target is replaced. A dangling leaf link is distinguished from an absent file
and rejected; directory destinations are also rejected. Temporary files always share the resolved destination's
parent/filesystem. Loading reads only the configured file, never orphan temporary files; startup scavenging,
concurrent writers and live external editing are outside scope.

`AtomicMoveNotSupportedException` becomes an `IOException` with message
`Atomic replacement is not supported for this data file.` and the original exception as its cause. A protection
capability failure uses `Cannot preserve the required file access protections.`. `AccessDeniedException` remains
an `AccessDeniedException`. Java makes existing-target replacement implementation-dependent even with
[`ATOMIC_MOVE`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...));
real existing-file replacement tests must pass on each supported platform. A provider that cannot replace safely
must fail; destructive fallback is not supported.

`LogicManager` saves after successful data-changing commands, including undo/redo and no-op mutations. Read-only
commands skip employee-file saving. Its generic save-error presentation is
`Could not save data due to the following error: <exception-message>`. Its permission presentation is
`Could not save data to file <exception-message> due to insufficient permissions to write to the file or the folder.`
The latter substitutes `AccessDeniedException.getMessage()`, including any paths and additional diagnostic text.
`BackupCommand` translates every `IOException` into
`Could not save the backup to <backup-path>. Check that you are allowed to write to that folder.`.
Backup/export commands write only their requested output files and do not trigger automatic employee-file saves.
For data-changing commands, including undo and redo,
`LogicManager` restores the checkpoint on save failure, preserving employee records, the complete history and the
effective view as well as the stored file. Read-only command view changes and completed backup/export files remain.

The pretty-printed UTF-8 schema remains an `employees` array containing `id`, `name`, `phone`, `email`, `department`,
`role`, `archived` and `managerId`. Employee order, raw values, archive status and manager are preserved. Missing
`archived` loads as active; missing or null `managerId` means no manager; empty arrays are valid; invalid employee data,
a manager that does not exist, self-reporting, reporting cycles and missing/null arrays are rejected; unknown properties
are ignored.
No history, transaction or schema-version fields are added.

The package-private `AtomicFileWriter.FileOperations` seam delegates to ordinary JDK operations. It exposes only
temporary creation, attribute views/writability, a write/force/close handle, atomic replacement and cleanup.
Writer tests inject faults there; storage tests verify serialization ordering and payloads; focused logic/backup
tests verify presentation. Package-private parser/storage factory hooks isolate backup test paths without changing
public constructors or command behavior. See the repository's `tests/test-plan.md` for coverage and release gates.

#### Unreadable data file

`MainApp#initModelManager` loads the employees through `Storage#readAddressBook`. If that throws a
`DataLoadingException`, such as for invalid JSON or invalid employee data, HuntR starts with an empty `ModelManager`
instead of failing to launch. Because the next successful data-changing command would overwrite the file, it first calls
`UnreadableDataFile#keepCopy`, which copies the file to `unreadable-` followed by its name in the same folder
(`data/unreadable-addressbook.json`), replacing any earlier copy.

`keepCopy` returns the warning that `MainApp` passes to `UiManager`, which shows it in the result box at startup.
The warning says where the copy is, or, if the copy could not be made, asks the user to close HuntR and back up the
file. A missing data file is not an error: HuntR starts with the sample employees and creates the file after the first successful data-changing command.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Reporting relationships and team structure

The reporting implementation is introduced in [PR #110](https://github.com/AY2627S1-CS2103T-W10-4/tp/pull/110).
Its developer documentation and the reporting sections in the [User Guide](UserGuide.md#assigning-a-manager-manager)
are maintained together in [PR #111](https://github.com/AY2627S1-CS2103T-W10-4/tp/pull/111).
The implementation described here requires #110; the documentation PR does not add the commands itself.

#### Model and relationship validation

`Employee` stores an optional direct manager as an immutable `EmployeeId`. `getManagerId()` exposes an `Optional`,
and `withManager()` returns a replacement employee while preserving all other fields, including archive status.
Existing constructors create employees without managers. Employee identity still depends only on the
case-insensitive employee ID; full equality and hashing also include the manager assignment.

`ReportingStructure` queries the complete roster, including archived employees. `withCanonicalManager()` resolves
a supplied manager ID to the spelling stored in the roster and rejects self-reporting or a missing manager.
`validate()` builds an ID lookup and follows manager links, tracking the current path and previously completed
paths to reject cycles, missing managers and self-reporting. Every employee has at most one manager; multiple
unconnected reporting trees and employees without managers are valid.

`AddressBook#addEmployee`, `setEmployee` and `setEmployees` validate the candidate roster before replacing stored
data and canonicalise manager references. A failed validation leaves the existing roster unchanged. Removing an
employee clears only their direct reports' manager assignments, including archived reports. Clearing active
employees retains archived employees and clears references to managers that were removed.

The Model class diagram above shows that an employee holds an optional manager ID rather than a direct reference
to another `Employee`. `AddressBook` delegates relationship checks to `ReportingStructure`.

The following sequence diagram shows the model update for an existing employee during manager assignment.
`ManagerCommand` first finds the employee in the full roster and creates a replacement using `withManager()`.
The diagram starts at `ModelManager#setEmployee` and shows validation before the stored roster is changed.
`ManagerCommand` converts a relationship validation failure into a `CommandException`. History commits, saving
and checkpoint restoration are handled separately by `LogicManager`, as described in
[Checkpoints and transaction boundary](#checkpoints-and-transaction-boundary).

<puml src="diagrams/ReportingValidationSequenceDiagram.puml" alt="Validation before storing a manager assignment, including rejection of invalid reporting relationships" />

#### Parsing and command execution

`AddressBookParser` routes the five reporting commands to their individual parsers. These delegate to
`ReportingParserSupport`, which rejects unknown parameters, duplicate prefixes, non-empty preambles, missing
values and invalid IDs, and appends command-specific usage to parse errors. `manager` requires both `id/` and
`m/`; the other four commands accept only `id/`.

`ReportingCommandSupport.find()` searches the complete roster by case-insensitive ID, independently of the
displayed list. `ManagerCommand` creates a replacement employee, resolves the manager, then calls
`model.setEmployee`; relationship validation failures become `CommandException`s. `UnassignManagerCommand`
replaces the employee with `withManager(null)`, succeeding even if no manager was assigned. `AddCommand` and
`EditCommand` also support `m/`: omitting it during an edit preserves the assignment, and an empty edit `m/`
removes it. An empty manager value on `add` or `manager` is rejected.

| Query command | Relationship selection |
| --- | --- |
| `team` | The target employee, their direct manager if assigned, and their direct reports. |
| `org` | All direct and indirect reports, excluding the target, using breadth-first traversal. |
| `chain` | Managers from the immediate manager to the root, excluding the target. |

`ReportingStructure.reports()` scans the roster for each visited manager; recursive queries take O(nk) time for
n employees and k visited managers. `chain()` builds an ID lookup and follows manager links in O(n + h) time for
a chain of height h. Both traversals track visited IDs. Result messages retain traversal order, while
`ReportingCommandSupport.show()` installs a refreshable reporting-query predicate without resetting the `SortedList`
comparator. Relationship changes, deletion, undo and redo recompute its ID membership against the current roster.
Employee cards therefore stay current while preserving the current sort order, or roster order when no sort is active.
These queries replace the current filter without modifying employee records. `Model#updateReportingEmployeeList`
applies the membership predicate without the normal active-only restriction, so both messages and cards include
related archived employees. Normal `list` and `find` continue to exclude archived employees.

#### Storage and presentation

`JsonAdaptedEmployee` persists the optional manager as `managerId`; a missing or null field means no manager,
so older valid JSON files remain readable. `JsonSerializableAddressBook#toModelType()` converts all employees
before calling `AddressBook#setEmployees`, allowing a manager to appear after their reports in the JSON array.
Duplicate IDs and invalid relationships reject the entire file through `IllegalValueException`; no partial
roster is loaded. Saved manager IDs use the existing manager's ID spelling.

`ExportCommand` writes eight CSV columns ending in `archived,managerId`; a blank manager ID means no assignment.
`ImportCommand` also accepts legacy six-column files and seven-column files ending in `archived`. After parsing
and checking IDs for duplicates, it resolves managers against the existing roster plus all imported rows and
validates the complete hierarchy before changing the model. It adds employees without managers first, then
sets their validated assignments, preserving CSV row order even when a manager appears later in the file.
`LogicManager` commits the import as one history step and restores its checkpoint on execution or save failure.

`ImportCommandTest` covers relationship round trips, forward references, legacy files and rejected relationships.

#### JSON backup restoration

`RestoreCommandParser` recognises `restore file/JSON_FILE_PATH`. `RestoreCommand` reads the complete backup through
`JsonAddressBookStorage#readBackup`, which validates all employee fields, unique IDs and the complete reporting graph
before the model is changed. Null employee entries and trailing JSON content are rejected as loading failures.
Backup reads use `JsonUtil.fromJsonStringStrict`; normal data and preferences reads keep their existing handling
of trailing content. The parser removes only the leading `file/`, preserving spaces and ` file/` within the path.
Only a validated backup is passed to `Model#setAddressBook`; the active view is then shown with the current sort.

Restore declares `HistoryAction.COMMIT`, so `LogicManager` saves it as one history step and rolls back the roster,
view and history if automatic saving fails. Undo and redo use snapshots and never reread the backup file.
An identical restore creates no history step, consistent with the existing no-op policy.

`EmployeeCard` and `EmployeeListCard.fxml` display the manager ID or `Unassigned`. Reporting usage is also listed
by `HelpCommand`. `ReportingCommandTest`, `ReportingCommandParserTest` and `ReportingStorageTest` cover command
behaviour, strict input validation and persistence; manual scenarios are provided below.

### Workforce statistics

`StatsCommand` reads the complete roster through `Model#getAddressBook()` and
`ReadOnlyAddressBook#getEmployeeList()`, then excludes archived employees. It reports the active employee count
and calls `appendBreakdown` for department and role. Each breakdown groups by the stored field value and sorts
by count descending, with ties ordered by label using `SortField.VALUE_ORDER`, the same order as `sort`. An empty active roster shows a total of
zero and `(none)` under each heading.

The sequence diagram below focuses on `StatsCommand#execute` after parsing. The command does not modify employee
records, the current filter, the sort order or undo/redo history. `LogicManager` skips employee-file saving for
this read-only command.

<puml src="diagrams/StatsSequenceDiagram.puml" alt="Interactions for summarising the active workforce" />

### Employee archiving

`Employee` has an immutable boolean archive status. Active and archived employees remain in the same
`UniqueEmployeeList`, so case-insensitive ID uniqueness, editing and deletion naturally apply regardless of status.
`archive id/EMPLOYEE_ID` and `unarchive id/EMPLOYEE_ID` replace the target with an otherwise identical employee whose
status is changed. They reject unknown IDs and redundant status changes without modifying the model.

#### Parsing and status changes

`ArchiveCommandParser` and `UnarchiveCommandParser` delegate to `SingleIdCommandParser`. Exactly one `id/` is
required; missing, blank, malformed or repeated IDs, unknown prefixes and unexpected preambles are rejected with
the corresponding command's usage. `ParserUtil.parseEmployeeId` enforces the 1-to-20-character ID format.
Both commands use `EmployeeCommandSupport.find` to search the complete roster by case-insensitive ID, including
employees hidden by the current view. Missing IDs produce `No employee with ID X was found.` without usage.
`ArchiveCommand` rejects an already archived employee; `UnarchiveCommand` rejects an already active employee.

`ModelManager#archiveEmployee` and `ModelManager#unarchiveEmployee` call `AddressBook#setEmployee` with a replacement
created by `Employee#withArchived(true/false)`. All other fields, roster position and reporting relationships are
preserved. After archiving, `ArchiveCommand` counts active direct reports across the complete roster. When the
count is non-zero, it appends a singular/plural notice and `Use team id/X to review their assignments.`, using
the stored manager ID. Archived and indirect reports are excluded from this count; no assignments are removed.

The diagram shows successful archiving after parsing. Unarchiving follows the same lookup and replacement flow,
using `withArchived(false)` and checking whether the employee appears in the resulting view instead of counting
reports. The shared checkpoint, history commit and save steps are described in
[Employee history and in-memory rollback](#employee-history-and-in-memory-rollback).

<puml src="diagrams/ArchiveSequenceDiagram.puml" alt="Archiving an employee while preserving their details and reporting relationships" />

#### Views and archived listing

Both operations preserve the installed filter predicate and sort comparator. Updating the existing observable
employee list lets the changed employee enter or leave the current view naturally, including empty search results
and archived-only views. `UnarchiveCommand` adds a `list` hint when the restored employee is outside the current view.

`ModelManager#updateFilteredEmployeeList` combines normal predicates with an active-status check, so `list`, `find` and `filter`
never expose archived employees. `list archived` calls `Model#showArchivedEmployees` to select only archived employees
in the same existing employee panel. `edit` preserves the target's archive status, while `delete` searches the complete
stored list and can remove either status; session history can restore the removed record.

`AddressBookParser` accepts only an empty argument string or the exact lowercase `archived` for `list`, after
trimming surrounding whitespace. It constructs `ListCommand(false)` or `ListCommand(true)`. Both reset the sort
comparator to roster order before installing their predicate. An empty archive displays
`There are no archived employees.` An empty active list with archived records displays the archived count and a
`list archived` hint. Listing does not commit a history step. Reporting views can include both active and archived
employees, while `StatsCommand` and `CheckCommand` operate only on active records regardless of the displayed view.

#### Persistence and history

`JsonAdaptedEmployee` persists the status as `archived` in each employee object. A missing field deserializes as `false`,
preserving compatibility with legacy data. `ClearCommand` calls `Model#clearEmployees`, which removes only active
employees and preserves archived entries in the same list.

Both status-changing commands return `HistoryAction.COMMIT`. `LogicManager` takes a checkpoint before execution,
commits one snapshot on success and saves before returning feedback. A handled execution or save failure restores
the data, history and view. Undo/redo preserves archive status and reporting relationships while retaining the
view in effect when it runs. Restart loads the saved archive statuses but starts with fresh undo/redo history.
`ArchiveCommandsTest`, `ArchiveViewIntegrationTest`, `ArchiveManagerFeedbackTest`, the individual parser tests and
`ListCommandTest` cover status changes, saved data, view preservation, redundant requests and listing feedback.

### Delete an employee by ID

`AddressBookParser` routes `delete` to `DeleteCommandParser`, which delegates argument validation to
`SingleIdCommandParser`. It accepts exactly one `id/` parameter and distinguishes unknown parameters, a missing
parameter, a blank ID, an invalid ID format and duplicate parameters. A preamble before `id/` is rejected as
unexpected arguments. Parsing errors include the command usage; see the
[User Guide](UserGuide.md#deleting-an-employee-delete) for messages and examples.

ID validation uses `ParserUtil.parseEmployeeId` and the existing `EmployeeId` rules: 1 to 20 letters, digits, hyphens or underscores, with no spaces. IDs retain their original casing and compare case-insensitively.

`DeleteCommand` calls `EmployeeCommandSupport.find(model, targetId)`, which searches the complete stored roster
using case-insensitive `EmployeeId` equality. The target may be active, archived or hidden by the current view.
An absent ID produces `No employee with ID X was found.` without usage, where `X` is the entered ID.

Before deletion, the command counts all direct reports, including archived employees. `ModelManager#deleteEmployee`
delegates to `AddressBook#removeEmployee`, which removes the target and replaces each direct report with
`employee.withManager(null)`. Other employee fields and relationships remain unchanged. The command returns
`Deleted employee:` followed by the employee's details, adding `1 direct report now has no manager.` for exactly
one report or `N direct reports now have no manager.` for two or more reports, where `N` is the count.
No notice is added when there are no direct reports. There is no confirmation step.

The installed filter predicate and sort comparator remain in place, including an archived-only or empty view.
The diagram below focuses on successful command execution after parsing. It omits the shared transaction wrapper:
`DeleteCommand#getHistoryAction()` returns `COMMIT`, so `LogicManager` captures a checkpoint before execution,
commits the whole deletion (including cleared manager assignments) as one history step, and saves before returning
success. Execution or save failure restores the checkpoint. See
[Employee history and in-memory rollback](#employee-history-and-in-memory-rollback) for the complete transaction.
Undo restores the employee's original fields, archive status, roster position and affected reporting relationships
during the same session; it preserves the view in effect when undo is run. Restart retains the saved deletion but
starts a new history.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Deleting an employee and clearing direct reports' manager assignments" />

### Edit an employee by ID

`EditCommandParser` requires exactly one `id/` parameter and at least one field: `n/`, `p/`, `e/`, `d/` or `r/`;
the reporting implementation also accepts `m/` as described above. It rejects unknown parameters, a non-empty
preamble and repeated prefixes. A missing ID or unexpected preamble produces `Invalid command format.` followed
by usage; an ID without any fields produces `At least one field to edit must be provided.`

`ParserUtil` validates the ID and supplied field values, returning the corresponding validation messages for invalid values. The parser stores validated changes in an `EditEmployeeDescriptor`; omitted fields keep their existing values. The ID only selects the employee and cannot be changed.

`EditCommand` searches `model.getAddressBook().getEmployeeList()` using case-insensitive `EmployeeId` equality, so filtered-out employees can be edited. It creates a replacement employee with the stored ID, including its original casing, and calls `model.setEmployee`. It then calls `updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES)` to show the full roster and returns `Edited employee: ` followed by the updated details. An unknown ID produces `No employee with ID X was found.` without usage, where `X` is the entered ID. Successful edits use the existing saving flow.

Before changing the model, `EditCommand` passes the replacement employee through `ReportingStructure#withCanonicalManager`,
which rewrites a manager ID to the spelling stored for that employee, so `m/e0001` is saved as `E0001`. It then compares
the replacement with the current employee. If they are equal, the command throws a `CommandException` with
`No changes made: the new values are the same as the existing ones for X.` and does not call `model.setEmployee`, so the
roster is not saved again and no entry is added to the undo history. The comparison is case-sensitive, so changing only
the letter case of a name counts as a change. An invalid manager (`An employee cannot report to themselves.`,
`Manager does not exist: X` or `Reporting relationships cannot contain a cycle.`) is reported through the same
`CommandException` path, leaving the roster unchanged.

`EditCommand#getHistoryAction` returns `HistoryAction.COMMIT`, so a successful edit is one step of `undo`.
The result message is built by `Messages#format`, which ends with `; Manager: ` followed by the manager ID when
the edited employee has a manager.

The sequence diagram below shows a successful `edit id/E0001 r/Manager`, adapted from the delete sequence diagram in the Logic component section. It focuses on parsing and model updates; `LogicManager` saves the address book before returning the result, as usual.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions for editing an employee by ID" />

### Find employees by name

`FindCommandParser` removes the spaces at both ends of the arguments and splits them into keywords at any kind of space (the characters matched by `[\\s\\p{Z}]`, which includes the non-breaking space of a web page). Empty arguments, and arguments beginning with `/`, produce `Invalid command format.` followed by the command usage. Each keyword is then checked against the existing `Name` validation rules; the first invalid keyword produces `Invalid keyword: X` followed by the keyword rules (`FindCommand#MESSAGE_INVALID_KEYWORD`). Otherwise, the parser creates a `NameContainsKeywordsPredicate` and passes it to `FindCommand`.

`NameContainsKeywordsPredicate` splits the employee's name and each keyword into words with `StringUtil.getSearchWords`, the same method that `filter` uses. Spaces and punctuation separate words, except for `&` and `'`, which stay in a word, and letter case and accents are ignored. A keyword matches when every one of its words is a whole word of the name, so `Tan` matches `Tan, Ah Kow`, `Mary-Ann` matches `Mary-Ann O'Brien`, and `emile` matches `Émile`, while `Han` does not match `Hans`. The employee matches when any keyword matches (OR semantics): `find alex david` matches either name, while `find al` does not match `Alex`. A keyword without any letter or digit never matches. It does not search employee IDs or other fields.

Because `find` and `filter` use the same word splitting, `find Tan` and `filter n/Tan` find the same employees.

`FindCommand` calls `model.updateFilteredEmployeeList(predicate)` to replace the current filter over the full employee list, without changing employee records. It returns the filtered list's size through `Messages.getEmployeesListedOverview`, using `1 employee listed!` or `N employees listed!`. No matches is a successful search returning `0 employees listed!`.

### Adding an employee

The `add` command creates an active `Employee` with six required fields and an optional manager ID. The flow is:

1. `AddressBookParser` delegates to `AddCommandParser`. `ArgumentPreprocessor` protects relation words such as
   `s/o` and `d/o` inside the name, then rejects unknown parameter prefixes. `ArgumentTokenizer` separates fields;
   the parser rejects missing required prefixes, a non-empty preamble and repeated prefixes, including `m/`.
2. `ParserUtil` validates each value and constructs the immutable field objects. Names, departments and roles
   are whitespace-normalised and Unicode NFC-normalised before validation. Invalid values raise `ParseException`
   before a command is executed; there is no partially constructed record in the model.
3. `AddCommand#execute` checks ID uniqueness across the full roster, then counts similar names as described below.
   `ReportingStructure.withCanonicalManager` resolves an optional manager against the full roster and rejects
   a missing manager or self-reporting. The model validates the candidate reporting structure before mutation.
4. `ModelManager#addEmployee` adds the employee and resets the filtered list to all active employees. The result
   contains `Messages.format(employeeToAdd)`, followed by a name warning only if there were existing matches.
5. `AddCommand#getHistoryAction` returns `COMMIT`. `LogicManager` owns the checkpoint, history commit and save;
   `AddCommand` does not save or commit directly. A command or save failure restores the checkpoint, including
   records, history and the previous view. The success result reaches the UI only after saving succeeds.

See [Employee history and in-memory rollback](#employee-history-and-in-memory-rollback) for the transaction boundary
and [Reporting relationships and team structure](#reporting-relationships-and-team-structure) for manager validation.

### Duplicate employee detection

`Employee#isSameEmployee` compares employee IDs only, using case-insensitive `EmployeeId` equality. Employees with IDs `E0001` and `e0001` are duplicates even if their other details differ. Matching names or contact details with different IDs do not make employees duplicates; `Employee#equals` separately compares all fields.

`UniqueEmployeeList` enforces this identity rule for all employees regardless of archive status. An operation that
would introduce duplicate IDs throws `DuplicateEmployeeException`. `AddCommand` first calls `model.hasEmployee(toAdd)`,
which includes archived employees. On a duplicate, `model.hasArchivedEmployee(toAdd)` selects the message:

* Active record: `An employee with Employee ID X already exists.`
* Archived record: ``An employee with Employee ID X already exists in the archive. Use `list archived` to see it.``

`X` is the supplied ID, preserving its case. Both outcomes throw `CommandException` before mutation. Otherwise,
a matching name only adds a warning to a successful addition. `Name#isSimilarTo` compares cached normalised names
using `equalsIgnoreCase`: whitespace is trimmed/collapsed and canonically equivalent Unicode accents are composed
with NFC. Accents and punctuation are preserved, so this is not edit-distance or accent-insensitive matching.
Matching archived employees have ` (archived)` appended to their IDs. The scan uses the underlying roster rather than
the filtered list, so a hidden or archived match cannot be missed, and the new employee is not counted as a match.

Each immutable `Name` caches its normalised form. Similar-name detection still scans the full roster, including
archived employees, in O(N) name comparisons (string-comparison cost also depends on name length). This avoids repeated
whitespace/Unicode normalisation during scans; it does not introduce a sublinear lookup. The warning retains at most
five IDs in roster order and reports the full count when more match, using O(1) additional ID-preview space.
`AddCommandPerformanceTest` exercises 10,000-employee rosters with no matches, all matches and a sixth match at the
end of the roster, checking exact counts, bounded previews and successful additions using the real `ModelManager`.
Its generous 10-second execution guard excludes fixture setup and is a severe-regression check, not a UI latency SLA.

Run `./gradlew -I tests/performance/history-performance.gradle addPerformance` for no-match, sparse-match and
all-match benchmarks at 100, 1,000 and 10,000 employees. The runner warms each workload and compares cached scans
with the former per-comparison normalisation, checking that both find the same number of matches. It separately
measures actual `AddCommand.execute` calls against the real model. Fixture setup, cleanup, disk saving, history
commit and UI rendering are excluded. The uncached scan is an emulated baseline, not a full older-version add.
Timing results are diagnostic and have no CI speedup threshold; see `tests/performance/results/2026-10-27-add.md`.

**Design considerations:** Employee IDs are the identity key because different people can share names and contact
details. Blocking a same-name addition would reject legitimate employees, so names produce an advisory warning.
Caching the normalised name is safe because `Name` is immutable; it moves normalisation work to construction and
retains an extra string per name. A maintained name index could reduce lookup work, but would need synchronisation
on add, edit, delete, import, load and history restoration. The current scan keeps those paths simple. It deliberately
continues after collecting five IDs to report the exact total, while the preview cap keeps the result message bounded.

**Regression coverage:** `AddCommandParserTest` covers field validation and repeated prefixes;
`NameTest` covers case/space similarity and composed/decomposed accents. `AddCommandIntegrationTest` uses the real
model for ID conflicts (including archived records after `clear`), same-name warnings, and hidden active/archived
matches. `AddCommandPerformanceTest` checks the exactly-five and more-than-five warning boundaries as well as
large rosters. These checks complement the adding-an-employee scenarios in the manual testing appendix.

When loading JSON, `JsonSerializableAddressBook#toModelType` converts and validates every employee. Duplicate IDs,
including IDs differing only in case, cause an `IllegalValueException`, rejecting the file rather than returning
partially loaded data.

### Filter employees by multiple criteria

`FilterCommandParser` first hides the slash in name words such as `s/o` and `d/o`, so that they are not read as
prefixes, and rejects any prefix other than `id/`, `n/`, `d/` and `r/` with `Unknown parameter`. It then tokenizes
the arguments and collects every value of each prefix. Text before the first prefix, or no prefix at all, produces
`Invalid command format.` followed by the usage. Each `id/` value must be a valid `EmployeeId`. Each `n/`, `d/` and `r/`
value is a search value, not a complete name, department or role, so it is not checked against the rules for those
fields. It only needs one word to search for, as given by `StringUtil#getSearchWords`; otherwise the parser reports
`The value of d/ should contain at least one letter or digit.` (or `n/`, `r/`) followed by the usage. The parser
passes the values to `EmployeeMatchesCriteriaPredicate` and returns a `FilterCommand`.

`EmployeeMatchesCriteriaPredicate` joins the criteria with `AND` and the values of one criterion with `OR`, which is
why a prefix that is repeated widens the search while a new prefix narrows it:

* An `id/` value is met when it equals the employee ID. `EmployeeId` equality ignores letter case.
* An `n/`, `d/` or `r/` value is met when every word of the value is a whole word of that field. Words are the
  lowercase runs of letters, digits, `&` and `'` in the text, so `d/Resources` matches `Human Resources` but
  `d/Engineer` does not match `Engineering`. The words of a value may be in any order.
* A criterion without values is not applied. The constructor asserts that at least one criterion has a value.

The sequence diagram below shows `filter d/Sales d/Finance r/Manager`, which is met by the managers who work in Sales
or in Finance.

<puml src="diagrams/FilterSequenceDiagram.puml" alt="Interactions for filtering employees" />

`FilterCommand#execute` calls `model.updateFilteredEmployeeList(predicate)`. `ModelManager` installs the predicate
on the `FilteredList` together with a check that the employee is not archived, so archived employees are never shown
by `filter`. The command replaces the earlier predicate of `find` or `filter` instead of adding to it. It does not
touch the comparator of the `SortedList`, so the sort order is kept. The result is the size of the displayed list,
reported by `Messages#getEmployeesListedOverview`. A filter with no matches succeeds with `0 employees listed!`.
`LogicManager` skips employee-file saving for this read-only command; the filter is never written to the JSON file.

#### Design considerations

**Aspect: how several criteria and several values are combined**

* **Alternative 1 (current choice):** `AND` between different fields, and `OR` between repeated values of one field.
  * Pros: Both searches that HR staff ask for are possible with one command, such as `filter d/Sales d/Finance r/Manager`.
    Repeating a prefix needs no new symbol, which is important because punctuation is a word separator in a value.
  * Cons: Repeating the prefix is a little longer to type than a list such as `d/Sales,Finance`.
* **Alternative 2:** Accept a separator inside a value, such as `d/Sales,Finance`.
  * Pros: Shorter to type.
  * Cons: The separator would also be a word separator in the value, so it would be ambiguous. Departments and roles
    may contain `,` themselves.

**Aspect: whether a new filter narrows the current list or replaces it**

* **Alternative 1 (current choice):** A new filter replaces the earlier one, as `find` does.
  * Pros: The result depends only on the last command, so it is easy to predict. All the fields are given in one
    command.
  * Cons: A user who wants to narrow the current list in steps must repeat the earlier criteria.
* **Alternative 2:** A new filter is applied to the list that is already shown.
  * Pros: Narrowing in steps is natural.
  * Cons: A mistake in a step can only be undone with `list` or `undo`, and `find`, `filter` and the reporting
    queries would need rules for how they combine.

### Sort employees

The model keeps the employees in three layers: the stored `UniqueEmployeeList`, the `FilteredList` that holds the
employees selected by the current predicate, and a `SortedList` over the `FilteredList`. `ModelManager#getFilteredEmployeeList`
returns the `SortedList`, so the UI, and every command that reads the displayed list, sees the filtered employees in
the sorted order. `SortCommand` therefore changes only the comparator of the `SortedList` through
`Model#updateEmployeeSortOrder`. The stored order and the saved file are not changed.

`SortCommandParser` splits the trimmed arguments on whitespace. No argument, or more than two words, produces
`Invalid command format.` followed by the usage. The first word is looked up with `SortField#fromString` and the
optional second word with `SortOrder#fromString`. Both ignore letter case. An unknown word produces
`Unknown sort field: X. The field must be one of: id, name, department, role.` or
`Unknown sort order: X. The order must be one of: asc, desc.`, each followed by the usage. Without a second word the
order is ascending.

<puml src="diagrams/SortSequenceDiagram.puml" alt="Interactions for sorting employees" />

`SortField#getComparator` builds the comparator. It compares the text of the field after removing accents and
ignoring letter case, and reverses that comparison for `desc`. Employees with the same value are then ordered by employee ID from A to Z in
both orders, so the order of the list never depends on the order in which employees were added. The tie-break never
applies to the `ID` field, because employee IDs are unique.

Because the `SortedList` observes the `FilteredList`, the list is kept in order without any further call. When an
employee is added, edited or deleted, or when `find` or `filter` changes the predicate, the list is reordered
automatically, and an employee whose name, department or role is edited moves to the correct position. Only
`ListCommand` clears the comparator, through `Model#resetEmployeeSortOrder`, to show the stored order again. The sort
order is not part of an undo/redo checkpoint and is not stored in the JSON file, so it is not remembered after HuntR is closed.

#### Design considerations

**Aspect: where the sorted order is kept**

* **Alternative 1 (current choice):** A `SortedList` over the filtered list, with the comparator as view state.
  * Pros: Sorting does not touch the stored employees, so it needs no saving, undo or rollback. The order is kept
    when employees change or the filter changes, with no extra code.
  * Cons: The order is lost when HuntR is closed, and is not restored by `undo`.
* **Alternative 2:** Sort the stored `UniqueEmployeeList` itself.
  * Pros: The order would be saved in the JSON file.
  * Cons: Every sort would be a change of the stored data, so it would need saving, a checkpoint for `undo`, and a
    way to get back the order in which employees were added.

### CSV import and export

`ImportCommand` and `ExportCommand` extend the abstract `CsvFileCommand`, which holds the file path and the folder
HuntR runs from. Both parsers use `ParserUtil.parseCsvFileArgument` to remove the `file/` parameter prefix and
reject blank paths. Only the leading prefix is removed, so spaces and ` file/` within a path are preserved.
Positional paths remain supported; `./file/staff.csv` explicitly addresses
a directory named `file`. `ParserUtil.parseCsvFilePath` then checks that the path is relative and ends in `.csv`, and
`CsvFileCommand#resolveFilePath` rejects a path that leads outside that folder once symbolic links are followed.
`CsvUtil` converts between a list of values and one CSV record, quoting values that contain commas, quotes or line breaks.
`CsvUtil.splitCsvRecords` splits only at line endings outside quoted values, preserves embedded line endings and
tracks the starting physical line of each record. `parseCsvLine` then validates each complete record's quote syntax.

`ExportCommand` writes the header in `ExportCommand.CSV_HEADER`, then one line per employee in the complete stored
roster, including archived employees. It creates missing parent folders and replaces an existing file.

The sequence diagram below shows a successful `import file/staff.csv`. `ImportCommand` reads the UTF-8 text, splits it into
complete records, checks the header against the three accepted column lists, and parses each non-blank record with
the same `ParserUtil` methods as `add`. A record that is invalid, repeats an earlier ID or uses an existing ID stops
the command with its starting physical line number, including when earlier records contain quoted line breaks.
`ReportingStructure.validate` then checks the managers of the existing and imported employees together. Only after
every check passes does the command change the model, so a failed import adds no one. It adds every employee without
a manager first and then sets the managers, so a manager may appear later in the file than their reports.

<puml src="diagrams/ImportSequenceDiagram.puml" alt="Interactions for importing employees from a CSV file" />

### Employee history and in-memory rollback

`ModelManager` owns a `VersionedAddressBook`, which extends `AddressBook` and inherits its ordinary equality and
hash-code behavior. `ModelManager` equality also remains unchanged: history is not compared.
Construction copies the supplied data and records exactly one initial snapshot with pointer zero.
History lasts only for the session. The JVM property `huntr.maxUndoHistory` configures the maximum undo steps;
`VersionedAddressBook(initialState, maxUndoHistory)` also accepts the limit explicitly. The default, `-1`, preserves
unlimited history. Non-negative values retain at most the limit plus one snapshots (including the current state);
zero disables undo. Invalid values are rejected at construction.
After a changed commit removes the redo tail and appends a snapshot, excess oldest states are removed and the pointer
is adjusted. No-op commits never evict snapshots or discard redo. Checkpoints retain the pre-command history so a
failed command can restore snapshots evicted by a tentative commit. Snapshots still share immutable employee objects;
the limit bounds retained states, not bytes, and does not limit the size of the employee roster itself.

The property is read by `configuredHistoryLimit()` when a `VersionedAddressBook` is constructed; it is not a JSON
preference and cannot be changed by an in-app command. Non-integer and out-of-range values fail integer parsing;
integers below `-1` fail constructor validation. A limit of zero retains the current snapshot for change detection
and rollback but exposes neither undo nor redo.

For a limit of two, let `S0` be the initial roster and `S1` through `S4` be distinct stored states:

| Operation | Retained snapshots, oldest first | Current state |
| --- | --- | --- |
| Construct | `S0` | `S0` |
| Commit `S1`, then `S2` | `S0, S1, S2` | `S2` |
| Commit `S3` | `S1, S2, S3` | `S3` |
| Undo | `S1, S2, S3` | `S2` |
| Commit unchanged `S2` | `S1, S2, S3` | `S2` (redo remains available) |
| Commit changed `S4` | `S1, S2, S4` | `S4` (redo of `S3` is discarded) |

Eviction happens only on changed commits, after truncating redo and appending the new snapshot. The capacity
applies to the entire retained sequence, including redo states, rather than a separate allocation for each direction.

`addressBookStateList` contains complete immutable `List<Employee>` snapshots. Containers cannot be changed;
immutable employee objects are shared. Every stored field, raw ID spelling, archive status and employee order is
retained, including hidden records. `commit()` compares ordered employee values and raw ID strings against the
current snapshot **before** removing redo states. Employee equality alone ignores ID case and is insufficient.
A changed state discards the redo tail and appends one snapshot; an identical state leaves history untouched.

The model exposes `commitAddressBook()`, `canUndoAddressBook()`, `canRedoAddressBook()`, `undoAddressBook()` and
`redoAddressBook()`. Each of `add`, `edit`, `delete`, `clear`, `import`, `archive`, `unarchive`, `manager` and
`unassign-manager` declares
`HistoryAction.COMMIT` through `Command#getHistoryAction()`. `LogicManager` commits once after successful command
execution, before saving. Commands do not call `commitAddressBook()` themselves. This single declaration supplies
both undo history and checkpoint/rollback protection. Import commits only after all rows have been added and the
command has returned. Low-level mutations do not commit.
`setAddressBook()` replaces working data without committing, resetting history or changing the predicate.

`UndoCommand` and `RedoCommand` check availability and move one step without committing. Snapshot data is restored
directly; redo never executes a command again or reads a CSV. Both commands preserve the current effective view.
An identical edit throws `CommandException` with `EditCommand.MESSAGE_NO_CHANGES` before model mutation; it does
not save, change the view or consume history. Clearing a roster with no active employees still resets to the active
view and returns its usual success message, then attempts saving, but creates no history entry. Both retain redo.
Read-only and failed commands also retain redo.

#### Checkpoints and transaction boundary

`Command#getHistoryAction()` defaults to `NONE`; modifying commands return `COMMIT`, and undo/redo return `NAVIGATE`.
`Command#isDataChanging()` is final and derives its result from that declaration. It means “this command may change
employee data or navigate employee history.” The ten modifying commands and both history commands therefore
return true even for no-ops. `LogicManager` parses first, captures a `ModelCheckpoint` for those commands, executes,
commits only for `COMMIT`, then saves through the existing storage API. `NAVIGATE` never creates a snapshot.
Only a successful storage return permits the result to be returned. No fallible work is added after that return.

<puml src="diagrams/CommandTransaction.puml" alt="Employee command execution, saving and rollback" />

A checkpoint copies the current employee-list container and shares a frozen history index, immutable snapshots,
the pointer and the effective predicate. Capture takes O(n) work for n current employees, independent of history
length. Navigation and no-op commits reuse the same index. The first changed commit after a checkpoint copies the
index before discarding redo or appending a snapshot; that commit still takes O(h) index-copying work for h snapshots.
Checkpoints remain usable after later changes, redo-tail removal and repeated restoration. Restoration updates the
working list and reinstalls the immutable history index without copying its entries. Shared snapshots never change.

`historyShared` records whether a checkpoint may hold the current index. Capture wraps an unshared index with
`Collections.unmodifiableList`; `ModelCheckpoint` retains that reference and copies only the working-list container.
Before a changed commit mutates an index marked shared, `VersionedAddressBook` creates a new `ArrayList` and clears
the flag. Restoring a checkpoint reinstalls its index and sets the flag again, so subsequent redo truncation or
capacity eviction cannot alter an older checkpoint. The working list is captured separately because low-level
mutations need not have been committed to the current snapshot yet.

**Design tradeoffs:** Copying the entire history index at every checkpoint makes capture grow with session length,
even for undo, redo and no-op commands. Sharing it moves that O(h) copy to changed commits while preserving rollback
after truncation and eviction. Complete roster snapshots simplify redo and multi-row import recovery, at the cost
of up to O(h × n) employee references for h snapshots of n employees; immutable employee values are shared.
The optional history limit bounds snapshot count. It does not make changed commits or disk saves constant-time.

Run `./gradlew -I tests/performance/history-performance.gradle checkpointPerformance` for an opt-in microbenchmark
at 10, 1,000 and 10,000 history steps with a fixed one-employee roster. It compares shared-index capture with the
former container-copying strategy and measures undo/redo command pairs using storage that performs no disk I/O.
It warms each scenario before reporting nanoseconds per operation. These diagnostic timings isolate history costs;
they are not end-to-end UI latency measurements and have no CI timing threshold. The recorded sample and baseline
limitations are in `tests/performance/results/2026-10-27-checkpoint.md`; decreasing sample times are not evidence
that a longer history makes undo/redo faster.

`CommandException`, `AccessDeniedException`, other `IOException` and unexpected `RuntimeException` during execution,
tentative commits, serialization or saving cause restoration for data-changing commands. Command exceptions and
runtime exceptions are rethrown; I/O failures retain their existing command-error conversion and use
`e.getMessage()` in these exact templates:

```text
Could not save data due to the following error: <exception-message>
```

```text
Could not save data to file <exception-message> due to insufficient permissions to write to the file or the folder.
```

The permission substitution is not necessarily just the configured path. `CommandBox` retains text and styles
`CommandException` and `ParseException` as errors; it clears text on success. It does not catch arbitrary runtime
exceptions. Parse failures occur before checkpoint capture and before any save. Syntax validation for undo/redo
rejects arguments before checking availability. See the User Guide for exact usage and boundary errors.

#### View and observable-list lifetime

The existing stored observable list and `FilteredList` instances are preserved. Checkpoints retain the installed
predicate reference, treating installed predicates as stable. Restore applies it directly with
`filteredEmployees.setPredicate(...)`; routing through `updateFilteredEmployeeList()` would add an active-only
condition and corrupt archived-only views. A zero-match search retains its predicate and can reveal a matching
employee restored by undo. Historical searches are not part of snapshots. The `SortedList` comparator remains in
place and is not captured in employee snapshots or checkpoints; undo/redo preserve the current sort. Selection,
scroll position and suppression of intermediate list notifications are outside scope.

#### Persistence and atomic saving

Only successfully executed data-changing commands save, including undo/redo and no-op mutations. Read-only commands
skip checkpointing and employee-file saving, so an unwritable employee file does not prevent help, searches or view
changes. Export/backup commands still write their requested files; those side effects are outside the transaction.
Only the current employee state is serialized in the existing `employees` JSON schema; there are no history,
checkpoint, command or predicate fields. Missing `archived` fields in older valid JSON still load as active;
validation, case-insensitive ID uniqueness, invalid-file handling, paths and preferences are unchanged. CSV export
uses eight columns including manager IDs; import also accepts legacy six-column and seven-column CSV and rejects
header-only files.

The combined boundary is the writer's successful atomic replacement. Employee changes and history commits are
tentative until then; a handled failure before replacement preserves file bytes and restores the checkpoint for
data-changing commands. No fallible work follows replacement, so a completed disk commit cannot be reported as a
failed save with memory rolled back. A failed no-op also restores its previous view and redo states. A failed new
change after undo restores the original redo sequence.

Restart loads the last successfully saved state as one initial snapshot with no available undo or redo.
External JSON editing while closed remains supported; live reload and concurrent writers are out of scope.

#### Validation and performance

`VersionedAddressBookTest` covers configured limits, zero/unlimited history, oldest-state eviction, no-op commits,
redo truncation and checkpoint restoration after eviction. Its long-history case commits 10,000 changes with a
25-step limit and checks that at most 26 snapshots remain. `ModelHistoryTest` checks shared index identity across
capture, navigation and no-ops, and verifies that changed commits preserve previously captured checkpoints.

`ModelHistoryTest`, `HistoryCommandTest` and `HistoryTransactionTest` also cover history traversal, no-ops, raw IDs,
checkpoints, view/list identity, import grouping, execution failures and save failures. Assertions compare ordered
stored values separately from model equality. Existing parser, storage, CSV and command tests cover compatibility.
The detailed matrix and recorded acceptance environment are in the repository's `tests/test-plan.md`.

For history performance, use the hardware conditions in [Non-functional requirements](#non-functional-requirements).
Start each of three measured sessions from the same 1,000-employee fixture. Perform 100 genuine edits while retaining
exactly 1,000 employees, then time all 100 undos and all 100 redos: 600 measured history operations in total. Measure
from command dispatch through saving to the first rendered UI frame showing the completed result and list update.
Every operation must complete within two seconds. Record the maximum and environment; keep thresholds out of CI
unit tests. Run the standalone UI acceptance harness described in `tests/test-plan.md` with atomic saving enabled.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is the sole HR administrator at a small or medium-sized company
* has no HR team or dedicated HR software, and keeps employee records in spreadsheets or shared files
* is the only person who maintains these records, on a single computer
* manages records for up to a few hundred employees
* often has to answer ad-hoc questions about individual employees, reporting lines and team sizes within minutes
* can type fast

**Value proposition**: HuntR helps the sole HR administrator keep employee records accurate, see how staff relate to each other, such as reporting lines and team membership, and get an overall picture of the workforce, all through typed commands.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a … | I can … | So that I can … |
|----------|--------|---------|-----------------|
| `* * *` | First-time user | See usage instructions and a list of available commands | Learn what the app does without reading a separate manual |
| `* * *` | First-time user | Be shown the correct command format when I mistype something | Correct my mistake without looking up the syntax |
| `* *` | Beginner user | Undo commands | Easily restore a correct state after I made a mistake |
| `* * *` | Beginner user | View the information I have about an employee | Answer questions concerning that employee |
| `*` | Expert user | Create command shortcuts | Save time on long winded commands that I frequently use |
| `*` | Expert user | View recent workforce changes | Stay aware of how the organisation is changing |
| `*` | Expert user | Compare department sizes | Understand how employees are distributed across the organisation |
| `*` | Expert user | View an employee’s relation to other employees | Understand how they fit into the organisation |
| `*` | Expert user | Record HR-related meetings or follow-up tasks | Keep track of actions related to employees |
| `*` | Expert user | View an overall workforce summary | Understand the state of the workforce without checking employees one by one |
| `*` | Expert user | Identify employees with missing information | Maintain complete records |
| `*` | Expert user | Record who an employee reports to | Understand the company’s reporting structure |
| `*` | Expert user | Update who an employee reports to | Keep records correct after a reorganisation or resignation |
| `* * *` | Long-time user | Easily search up contact details with the commands | Save time instead of using traditional address / contact books |
| `* *` | Long-time user | Import employee data from a supported file | Initialise or restore the company’s records efficiently |
| `* *` | Long-time user | Filter employees by search criteria | Gather specific organisation-wide data efficiently |
| `* * *` | Basic user | Add an employee's information | the app reflects my company's workforce |
| `* * *` | Basic user | List all employees | see the full roster in one place |
| `* * *` | Basic user | Delete an active or archived employee by ID, even when hidden by a search | Remove the intended record without changing my view |
| `* *` | Careful user | Undo a deletion and its reporting changes during the same session | Recover a mistakenly deleted employee and their relationships |
| `* * *` | Basic user | Find an employee by name | quickly retrieve their information without browsing the whole list |
| `* * *` | Basic user | Have my data saved automatically after every change | I don't lose records if the app closes unexpectedly |
| `* * *` | Basic user | Exit the application with a command | close it safely knowing my data is saved |
| `* *` | Basic user | Edit an employee's details | keep records accurate when someone's role or contact info changes |
| `* *` | Careful user | **Coming soon:** Be asked to confirm before a record is deleted | avoid losing a record to a mistyped command |
| `* *` | Busy user | Filter employees using multiple criteria at once (e.g. department and role) | narrow down results faster than one field at a time |
| `* *` | Busy user | View an entire team's roster with one command | prepare for a team meeting without assembling the list myself |
| `*` | Busy user | Sort employees by a chosen field (e.g. name or department) | scan records in the order that's useful to me |
| `*` | Busy user | Look up an employee's leave status with one command | check quickly if they're available for a meeting |
| `*` | Long-time user | Identify employees who don't appear connected to anyone else in the organisation | investigate whether my workforce information is incomplete |
| `*` | Long-time user | Count employees by department | quickly see the size of each team |
| `* *` | HR administrator | Archive an employee while retaining their details and reporting relationships | Keep former employees' records outside the active roster |
| `* *` | HR administrator | List archived employees and restore one by ID | Bring an employee back without re-entering their information |
| `* *` | HR administrator | Retain my search and sort order when archiving or restoring employees | Continue reviewing the same group of records |

### Use cases

(For all use cases below, the **System** is `HuntR` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Add an employee**

**MSS**

1. User requests to add an employee and provides the employee ID, name, phone number, email, department, and role, optionally assigning an existing employee as manager.
2. HuntR validates the provided details.
3. HuntR adds the employee and saves the updated employee records.
4. HuntR displays the added employee's details and shows all active employees.

   Use case ends.

**Extensions**

* 1a. A required field is missing.

  * 1a1. HuntR shows an error message and the expected command format.

    Use case resumes at step 1.

* 2a. One or more provided fields are invalid.

  * 2a1. HuntR shows an error message describing the invalid input.

    Use case resumes at step 1.

* 2b. An active or archived employee with the same employee ID already exists (ignoring case).

  * 2b1. HuntR rejects the addition and identifies the conflicting ID. For an archived record, it also directs the user to the archived list.

    Use case ends.

* 3a. HuntR is unable to save the updated employee records.

  * 3a1. HuntR restores the previous records and view, and informs the user that the employee could not be saved successfully.

    Use case ends.

* 4a. One or more existing active or archived employees have matching names after case and space normalisation.

  * 4a1. HuntR appends a warning with up to five matching IDs, marking archived matches and reporting the total
    when more than five match. The new employee has still been added successfully.

    Use case ends.

**Use case: Edit an employee**

**MSS**

1. User requests to edit an employee using `edit id/EMPLOYEE_ID` with at least one field to change.
2. HuntR validates the ID and the given fields.
3. HuntR finds the employee in the complete roster, comparing IDs case-insensitively.
4. HuntR updates the employee, saves the updated records and shows the complete roster.
5. HuntR displays the updated employee's details as confirmation.

    Use case ends.

**Extensions**

* 1a. The ID is missing, the command contains an unknown or repeated parameter, or it has unexpected text before the first parameter.

  * 1a1. HuntR identifies the input error and shows the command usage. Records remain unchanged.

    Use case resumes at step 1.

* 1b. The user does not provide any field to update.

  * 1b1. HuntR informs the user that at least one field must be provided.

    Use case resumes at step 1.

* 2a. One or more provided fields are invalid.

  * 2a1. HuntR shows an error message describing the invalid input. Records remain unchanged.

    Use case resumes at step 1.

* 3a. No employee has the specified employee ID.

  * 3a1. HuntR displays `No employee with ID X was found.`, using the entered ID for `X`. Records remain unchanged.

    Use case resumes at step 1.

* 3b. The employee is hidden by the current search.

  * 3b1. HuntR edits that employee in the complete roster.

    Use case resumes at step 4.

* 3c. Every given value is the same as the employee's current value.

  * 3c1. HuntR displays `No changes made: the new values are the same as the existing ones for X.`. Nothing is saved or added to the undo history.

    Use case resumes at step 1.

* 3d. The manager in `m/` does not exist, is the employee themselves, or would create a reporting cycle.

  * 3d1. HuntR shows an error message that names the problem. Records remain unchanged.

    Use case resumes at step 1.

* 4a. HuntR is unable to save the updated employee records.

  * 4a1. HuntR informs the user that the employee changes could not be saved successfully and restores the previous records.

    Use case ends.

**Use case: Find employees**

**MSS**

1. User requests to find employees using one or more name keywords.
2. HuntR searches the employee records for names containing at least one of the keywords.
3. HuntR displays the matching employees as a numbered list and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. The user does not provide a keyword.

  * 1a1. HuntR informs the user that a keyword is required and shows the correct command format.

    Use case resumes at step 1.

* 2a. No employee matches any of the keywords.

  * 2a1. HuntR displays an empty result list and informs the user that no employees were found.

    Use case ends.

**Use case: Filter employees**

**MSS**

1. User requests to filter employees by one or more of employee ID, name, department and role.
2. HuntR finds the employees who match all the given fields.
3. HuntR displays the matching employees as a numbered list and reports the number of matches.

    Use case ends.

**Extensions**

* 1a. The user gives no field, gives text before the first field, or gives an unknown field.

  * 1a1. HuntR shows an error message and the expected command format.

    Use case resumes at step 1.

* 1b. A value is empty, has no letter or digit, or is not a valid employee ID.

  * 1b1. HuntR shows an error message that names the field, and the expected command format.

    Use case resumes at step 1.

* 1c. The user repeats a field, such as `d/Sales d/Finance`.

  * 1c1. HuntR matches the employees who match any value of that field, and all the other fields.

    Use case resumes at step 3.

* 2a. No employee matches.

  * 2a1. HuntR displays an empty list and reports `0 employees listed!`.

    Use case ends.

**Use case: Sort employees**

**MSS**

1. User requests to sort the displayed employees by a field, and optionally by an order.
2. HuntR orders the displayed employees by the field.
3. HuntR displays the employees in the new order and reports the field and the order.

    Use case ends.

**Extensions**

* 1a. The field or the order is missing, unknown, or followed by extra text.

  * 1a1. HuntR shows an error message and the expected command format.

    Use case resumes at step 1.

* 1b. The user does not give an order.

  * 1b1. HuntR uses ascending order.

    Use case resumes at step 2.

* 2a. Several employees have the same value for the field.

  * 2a1. HuntR orders them by employee ID from A to Z.

    Use case resumes at step 3.

**Use case: View employee data**

**MSS**

1. User requests to view all employee records.
2. HuntR displays the employees as a numbered list with summary information.
3. User selects a specific employee from the displayed list.
4. HuntR displays the employee's full details, including their employee ID, department, role.

    Use case ends.

**Extensions**

* 2a. There are no employee records.

  * 2a1. HuntR displays an empty list and informs the user that there are no employees to show.

    Use case ends.

* 3a. The selected index does not correspond to an employee in the displayed list.

  * 3a1. HuntR informs the user that the displayed index is invalid.

    Use case resumes at step 2.

**Use case: Archive an employee**

**MSS**

1. User enters `archive id/EMPLOYEE_ID`.
2. HuntR validates the ID and finds an active employee in the complete roster.
3. HuntR marks the employee archived, preserving their other fields and reporting relationships, and saves the change.
4. HuntR displays the employee's details and any active-direct-report notice, retaining the current view and sort order.

    Use case ends.

**Extensions**

* 1a. Input has a missing, blank, invalid or repeated ID, an unknown prefix or unexpected arguments.
  * 1a1. HuntR reports the input error and command usage without changing data or the view.
    Use case resumes at step 1.
* 2a. The ID is unknown or the employee is already archived.
  * 2a1. HuntR reports the relevant error without changing data, history or the view.
    Use case ends.
* 3a. Saving fails before the data file is replaced.
  * 3a1. HuntR restores the records, history and view and displays the save error.
    Use case ends.
* 4a. The employee no longer matches the current view.
  * 4a1. The employee disappears from that view; the predicate remains selected even if there are no matches.
    Use case ends.

**Use case: Restore an archived employee**

**MSS**

1. User enters `unarchive id/EMPLOYEE_ID`.
2. HuntR validates the ID and finds an archived employee in the complete roster.
3. HuntR marks the employee active, retaining their other fields and reporting relationships, and saves the change.
4. HuntR displays the employee's details and retains the current view and sort order.

    Use case ends.

**Extensions**

* 1a. Input is malformed.
  * 1a1. HuntR reports the specific error and `unarchive` usage, leaving data and the view unchanged.
    Use case resumes at step 1.
* 2a. The ID is unknown or the employee is already active.
  * 2a1. HuntR reports the relevant error without changing data, history or the view.
    Use case ends.
* 3a. Saving fails before the data file is replaced.
  * 3a1. HuntR restores the records, history and view and displays the save error.
    Use case ends.
* 4a. The restored employee is outside the current view, including an archived-only view.
  * 4a1. HuntR suggests `list` to see the employee; any remaining archived records stay visible in an archive view.
    Use case ends.

**Use case: List archived employees**

**MSS**

1. User enters `list archived`.
2. HuntR replaces the current view with all archived employees in roster order.
3. HuntR displays `Listed all archived employees.` Employee data and undo/redo history are unchanged.

    Use case ends.

**Extensions**

* 1a. The argument differs from lowercase `archived` or includes additional text.
  * 1a1. HuntR reports `Invalid command format.` and `Usage: list [archived]`, leaving the view unchanged.
    Use case resumes at step 1.
* 2a. There are no archived employees.
  * 2a1. HuntR displays an empty panel and `There are no archived employees.`
    Use case ends.

**Use case: Delete an employee**

**MSS**

1. User requests to delete an employee using `delete id/EMPLOYEE_ID`.
2. HuntR validates the ID and finds the employee in the complete roster, comparing IDs case-insensitively.
3. HuntR immediately deletes the specified employee record and saves the updated records, preserving the current view and sort order.
4. HuntR displays the deleted employee's details as confirmation.

    Use case ends.

**Extensions**

* 1a. The parameter is missing or unknown, the ID is blank or malformed, or the command contains repeated parameters or unexpected arguments.

  * 1a1. HuntR identifies the specific input error and shows the command usage. Records remain unchanged.

    Use case resumes at step 1.

* 2a. No employee has the specified employee ID.

  * 2a1. HuntR displays `No employee with ID X was found.`, using the entered ID for `X`. Records remain unchanged.

    Use case ends.

* 3a. The employee is archived or hidden by the current search or filter.

  * 3a1. HuntR deletes that employee from the complete roster while retaining the current view, even if it becomes empty.

    Use case resumes at step 4.

* 3b. The employee has direct reports.

  * 3b1. HuntR clears those employees' manager assignments, including archived reports, as part of the same change.
  * 3b2. HuntR adds the affected direct-report count to the deletion confirmation.

    Use case ends.

* 3c. Saving the deletion fails before the data file is replaced.

  * 3c1. HuntR restores the employee records, reporting relationships, history and view and displays the save error.

    Use case ends.

*{More to be added}*

### Non-Functional Requirements

The following requirements describe the intended HuntR product, including capabilities that are not yet implemented. Performance limits are acceptance targets, not measured results for the current version. Platform, packaging, storage, and display requirements incorporate the applicable [course project constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

HuntR is intended for one HR administrator managing workforce records locally. Shared access to the same data by different users, including taking turns on a shared installation, is outside the supported usage.

1. **NFR-01: Platform compatibility.** HuntR must launch and support its employee-management commands on Windows, Linux, and macOS with Java `25` installed, without requiring another Java version. Verify the same release on each operating system by adding, listing, finding, and deleting an employee, then exiting and relaunching.

2. **NFR-02: Portable distribution.** HuntR must be distributed as a single executable JAR containing its application dependencies and resources. With Java `25` already installed, a user must be able to copy the JAR into a writable folder and launch it using `java -jar huntr.jar`, without an installer or separate library installation.

3. **NFR-03: Offline operation.** Adding, listing, finding, deleting, saving, and reloading employee records must work with the network disconnected. These operations must not require a remote server or an online account, and HuntR must not transmit employee records over the network.

4. **NFR-04: Human-editable storage.** Workforce records must be stored locally in a human-readable, human-editable JSON file, without a database management system. When the application is closed, a user must be able to edit the file in a text editor; valid changes that satisfy the employee field and uniqueness constraints must appear on the next launch. When launched from the JAR's folder, the default file is `data/addressbook.json` in that folder.

5. **NFR-05: Capacity.** HuntR must support at least 1,000 valid employee records with distinct employee IDs. Saving and reloading a dataset of this size must preserve every record and its employee ID, name, phone number, email, department, and role. This is a minimum supported capacity, not a limit on the employee ID format.

6. **NFR-06: Command response time.** Each `add`, `list`, `find`, and `delete` command must display its result and finish updating the employee list within 2 seconds of pressing Enter, including any automatic save. Verify 20 executions of each command under the performance conditions below, including searches with zero matches and searches matching all employees; every execution must meet the limit.

7. **NFR-07: Startup time.** HuntR must load the saved records, display the employee list, and accept command input within 5 seconds of starting the JAR process under the performance conditions below. Verify this over five separate launches; every launch must meet the limit.

8. **NFR-08: Keyboard usability.** After launch, an HR administrator must be able to add an employee, list employees, find an employee, delete an employee, access help, and exit using only the keyboard. Each of `add`, `list`, `find`, and `delete` must accept its required input in one command submission, without requiring mouse interaction or a sequence of input dialogs.

9. **NFR-09: Recoverable input errors.** Rejected commands with invalid syntax, invalid field values, a duplicate employee ID, or a nonexistent target employee ID must leave stored records and the displayed employee list unchanged. The application must remain open and accept the next valid command. The English error message must identify the problem and provide the expected command format or field constraint where applicable.

10. **NFR-10: Persistence reliability.** A data-changing command must save its changes before reporting success. After a successful command followed by normal shutdown and relaunch, all employee records and field values must match the saved state without a manual save. Verify additions and deletions, including deletion of the last employee. If saving fails, such as because the data folder is not writable, HuntR must report the save failure instead of reporting success.

11. **NFR-11: Invalid-file handling.** If the employee data file contains malformed JSON or an employee that violates the field or uniqueness constraints, HuntR must reject the whole dataset, start with an empty employee list, and record a warning in the log. It must not load only the valid entries from that file. A missing or malformed preferences file must cause HuntR to use default preferences without preventing startup.

12. **NFR-12: Display usability.** At resolutions of 1920 x 1080 and higher with 100% or 125% display scaling, the command box, result display, and employee list must remain accessible, with their text readable directly or through scrolling. All functions must also remain usable at resolutions of 1280 x 720 and higher with 150% scaling. Verify each resolution/scaling combination with the application maximised, including long employee details and error messages.

**Performance verification conditions:** Use Java `25`, a computer with at least two CPU cores and 8 GB RAM, a local SSD, and no other resource-intensive applications running. Use 1,000 valid employee records with distinct IDs and up to 100 characters in each other text field. Restore this dataset before each command trial. Record the operating system, processor, Java version, dataset, and timings with the test results. These fixture sizes do not introduce new field-validation limits.

### Glossary

These definitions describe HuntR's employee-management domain. The inherited codebase still uses names such as `Person` and `AddressBook` for the underlying records and their collection.

| Term | Definition |
|------|------------|
| **AB3** | AddressBook Level 3, the contact-management application from which HuntR is adapted. |
| **Command-line interface (CLI)** | An interface controlled by typing text commands. In HuntR, commands are entered in the GUI's command box and submitted with Enter. |
| **Data file** | The local JSON file containing workforce records. The default is `data/addressbook.json`, relative to the application's working directory. It is separate from the preferences file. |
| **Data persistence** | Saving records to disk so that they remain available after the application closes and are restored in a later session. |
| **Department** | The organisational unit to which an employee belongs, such as Engineering or Finance; supplied using `d/`. |
| **Displayed index** | An employee's position in the currently displayed list, starting at 1. It can change when the list changes and is distinct from the employee ID. HuntR's specified `delete` command uses the employee ID. |
| **Duplicate employee record** | A record with the same employee ID as another record. Two employees with the same name but different employee IDs are distinct records. |
| **Employee** | A member of the organisation whose information the HR administrator manages in HuntR. An employee record contains an employee ID, name, phone number, email, department, and role. |
| **Employee ID** | The unique identifier for an employee record: 1 to 20 letters, digits, hyphens or underscores, with no spaces, such as `E0123`, `EMP-0042` or `2024-017`. It is stored as typed and compared case-insensitively. It is supplied using `id/` and distinguishes employees even when their names are identical. |
| **Filtered employee list** | The subset of stored employee records currently displayed after applying search criteria. Filtering changes the view without deleting records from the workforce roster. |
| **Graphical user interface (GUI)** | The application's visual interface, including the command box, result display, and employee list. |
| **HR administrator** | The human resources staff member who operates HuntR to maintain the organisation's employee records; the application's target user. |
| **JAR** | Java Archive: the executable package containing HuntR's application code, dependencies, and resources. |
| **JSON** | JavaScript Object Notation, the text-based format used for employee data and preferences. |
| **Keyword** | A space-separated search term supplied to `find`. It matches a whole word in an employee's name, ignoring letter case. With multiple keywords, a match on any one keyword is sufficient. |
| **Main success scenario (MSS)** | The sequence of steps in a use case when the interaction succeeds without taking an error or alternative path. |
| **Preferences file** | The local `preferences.json` file containing application settings, such as window size and position, rather than employee records. |
| **Reporting relationship** | An optional link from an employee to their direct manager, identified by employee ID. Manager references must exist, and self-reporting and cycles are rejected. |
| **Role** | An employee's job title, such as Software Engineer or Accountant; supplied using `r/`. It does not denote application access permissions. |
| **Session** | One period of application use, from launch until shutdown. |
| **Workforce records / roster** | The complete collection of employee records stored in HuntR, including employees not visible in the current filtered list. |

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

**Prerequisites:** Download the release JAR named `huntr.jar` and place it in an empty, writable folder. Open a terminal in that folder and ensure Java 25 is installed.

1. Run `java -jar huntr.jar`.<br>
   Expected: The HuntR GUI opens and displays six sample employees. Each employee has an employee ID, name, phone number, email, department and role. The initial window size may not be optimal.
2. Resize the window, move it to a different location, and close it using the window close button.<br>
   Expected: HuntR closes and a `preferences.json` file is created or updated in the folder containing `huntr.jar`.
3. Run `java -jar huntr.jar` again.<br>
   Expected: The GUI opens at the most recently saved size and location, with the employee records retained.
4. Enter `exit` in the command box.<br>
   Expected: HuntR closes normally.

### Adding an employee

**Prerequisites:** Launch HuntR in a disposable writable folder with the sample employees. IDs `E1001` through
`E1004` and `E1101` through `E1107` must be unused in both the active and archived rosters. No existing employee
should be named `Mary-Ann O'Brien` or `Jose Tan` (ignoring case and extra spaces). Run these steps in order.
For each rejected command, verify that no record is added or changed.

1. Enter `add id/E1001 n/Mary-Ann O'Brien p/+65 6123-4567 (HP) e/mary.ann@example.com d/Research & Development r/Software Engineer (Backend)`.<br>
   Expected: HuntR displays `New employee added:` followed by all six details. The employee appears in the list with ID `E1001`.
2. Enter `add id/e1001 n/Another Employee p/91234567 e/another@example.com d/Operations r/Analyst`.<br>
   Expected: HuntR reports that an employee with ID `e1001` already exists. No employee is added because employee IDs are compared case-insensitively.
3. Enter `add id/E1002 n/  mary-ann   o'brien  p/81234567 e/mary2@example.com d/Finance r/Analyst`.<br>
   Expected: HuntR adds `E1002`, normalises repeated spaces in the name, and displays a warning that an employee with the same or a very similar name already exists. `E1001` remains unchanged.
4. Add six employees named `Jose Tan` with IDs `E1101` through `E1106`, each with a distinct valid phone number and email. After adding `E1105`, check that its warning lists exactly `E1101` through `E1104`. After adding `E1106`, check
   that the warning lists all five IDs `E1101` through `E1105` without the total-count wording. Then enter
   `add id/E1107 n/ JOSE   tan p/91231107 e/jose7@example.com d/Finance r/Analyst`.<br>
   Expected: HuntR adds `E1107` and warns that `6 employees with the same or a very similar name already exist`, showing only the first five matching employee IDs. The warning points to `find NAME` and `list archived` for the remaining matches.
5. Enter `add id/E1003 n/Test User p/12 e/test@example.com d/QA r/Tester`.<br>
   Expected: HuntR reports that phone numbers must contain at least 3 digits. No employee is added.
6. Enter `add id/E1003 n/Test User p/91234567 e/test@example.com d/QA`.<br>
   Expected: HuntR reports an invalid command format and shows the `add` command usage because the required `r/` field is missing. No employee is added.
7. Enter `archive id/E1001`, then `find NobodyMatchesThisName`, then repeat the command from step 2.<br>
   Expected: HuntR displays ``An employee with Employee ID e1001 already exists in the archive. Use `list archived` to see it.``
   The empty search view is preserved. `list archived` still shows the original `E1001`.
8. Enter `list archived`, then `add id/E1004 n/MARY-ANN O'Brien p/81234568 e/mary4@example.com d/Finance r/Analyst`.<br>
   Expected: HuntR adds `E1004` and switches to all active employees. The warning includes `E1001 (archived)` and
   `E1002`. `E1001` remains archived. Restart HuntR and check that the new record and archive status are retained.
9. Enter `add id/E1003 n/Test User p/91234567 e/test@example.com d/QA r/Tester n/Other Name`.<br>
   Expected: HuntR rejects the repeated `n/` parameter rather than silently using its last value.
10. Enter `add id/E1003 n/Test User p/91234567 e/test@example.com d/QA r/Tester x/value`.<br>
    Expected: HuntR reports `Unknown parameter: x/` and the command usage. Repeat without `x/value` but with an
    empty `n/` value; HuntR reports `Name should not be blank.`

### Listing employees

**Prerequisites:** Launch HuntR with at least two employees whose names are different.

1. Enter `find Alex`.<br>
   Expected: Only employees whose names contain the whole-word keyword `Alex`, ignoring case, are displayed.
2. Enter `list`.<br>
   Expected: HuntR displays `Listed all active employees.` and restores the complete active employee roster.
3. Enter `clear`, followed by `list`.<br>
   Expected: After `clear`, all active employee records are removed. If archived records remain, `list` displays
   an empty active roster and suggests `list archived` with their count. Otherwise, the message begins with
   `You don’t have any employees yet.`, followed by the `add` command format.

### Finding employees

**Prerequisites:** Launch HuntR with the original sample employees, including `Alex Yeoh`, `David Li` and `Charlotte Oliveiro`.

1. Enter `find alex`.<br>
   Expected: Only `Alex Yeoh` is displayed and HuntR reports `1 employee listed!`. Matching is case-insensitive.
2. Enter `find Alex David`.<br>
   Expected: `Alex Yeoh` and `David Li` are displayed and HuntR reports `2 employees listed!`. An employee matches when its name contains any one of the whole-word keywords.
3. Enter `find NotPresent`.<br>
   Expected: The employee list is empty and HuntR reports `0 employees listed!`.

   **With names that contain punctuation and accents:** first add `add id/T1 n/Tan, Ah Kow p/91110001 e/t1@example.com d/Sales r/Analyst` and `add id/T3 n/Émile Roux p/91110003 e/t3@example.com d/Sales r/Analyst`.
   * Enter `find Tan`.<br>
     Expected: `Tan, Ah Kow` is displayed (the comma does not matter) and HuntR reports `1 employee listed!`.
   * Enter `find emile`.<br>
     Expected: `Émile Roux` is displayed (accents are ignored).
   * Enter `find Ah, Alex`.<br>
     Expected: `Tan, Ah Kow` and `Alex Yeoh` are displayed, and HuntR reports `2 employees listed!`.
4. Enter `find`, with no keyword.<br>
   Expected: HuntR reports an invalid command format and shows the `find` command usage. The previously displayed empty result remains unchanged.
5. Enter `find /Alex`.<br>
   Expected: HuntR reports an invalid command format and shows the `find` command usage because a keyword cannot begin with `/`.
6. Enter `find Alex valid!`.<br>
   Expected: HuntR reports `Invalid keyword: valid!` followed by the keyword rules because `!` is not allowed. The employee list and stored data remain unchanged.

### Editing an employee

**Prerequisites:** Launch HuntR with the original sample employees. Confirm that `E0001` is `Alex Yeoh` and `E0002` is `Bernice Yu`.

1. Enter `edit id/e0001 p/+65 6123-4567 (Office) d/Research & Development r/Principal Engineer`.<br>
   Expected: HuntR displays `Edited employee:` followed by the complete updated record. The stored ID remains `E0001`, the three supplied fields are updated, and the name and email remain unchanged.
2. Enter `find Alex`, followed by `edit id/E0002 n/Bernice Tan`.<br>
   Expected: `E0002` is edited even though it was hidden by the search. HuntR then displays the complete roster, with `E0002` renamed to `Bernice Tan`.
3. Enter `edit id/E9999 n/Missing Employee`.<br>
   Expected: HuntR displays `No employee with ID E9999 was found.` and no employee is changed.
4. Enter `edit id/E0001`.<br>
   Expected: HuntR displays `At least one field to edit must be provided.` and no employee is changed.
5. Enter `edit id/E0001 p/12`.<br>
   Expected: HuntR reports that phone numbers must contain at least 3 digits and leaves `E0001` unchanged.
6. Enter `edit id/E0001 p/91234567 p/81234567`.<br>
   Expected: HuntR reports multiple values for `p/` and leaves `E0001` unchanged.
7. Enter the command of step 1 a second time.<br>
   Expected: HuntR displays `No changes made: the new values are the same as the existing ones for E0001.` and `E0001` is unchanged. Enter `undo`: it reverts the rename of `E0002` from step 2, not a second edit of `E0001`. Enter `redo` to bring the rename back.
8. Enter `edit id/e0002 m/e0001`.<br>
   Expected: HuntR displays `Edited employee:` followed by the record of `E0002`, ending with `; Manager: E0001`. The manager ID is stored as `E0001`, not as typed.
9. After step 8, enter `edit id/E0002 m/E0001`.<br>
   Expected: HuntR displays `No changes made: the new values are the same as the existing ones for E0002.`
10. After step 8, enter `edit id/E0001 m/E0002`, `edit id/E0001 m/E0001` and `edit id/E0002 m/E9999` separately.<br>
    Expected: HuntR displays `Reporting relationships cannot contain a cycle.`, `An employee cannot report to themselves.` and `Manager does not exist: E9999` respectively, and no employee is changed.
11. After step 8, enter `edit id/E0002 m/`.<br>
    Expected: HuntR displays `Edited employee:` followed by the record of `E0002` without a `Manager:` part. Enter `undo` to restore the manager.


### Filtering employees

**Prerequisites:** Launch HuntR with the original sample employees, then enter `list`. `E0001` and `E0004` are in the
Engineering department, `E0003` is in Finance and `E0006` is in Sales.

1. Enter `filter d/Engineering`.<br>
   Expected: `Alex Yeoh` and `David Li` are displayed and HuntR reports `2 employees listed!`.
2. Enter `filter d/Engineering r/Manager`.<br>
   Expected: Only `David Li` is displayed and HuntR reports `1 employee listed!`. An employee must match both fields.
3. Enter `filter d/Sales d/Finance`.<br>
   Expected: `Charlotte Oliveiro` and `Roy Balakrishnan` are displayed. A repeated field matches any of its values.
4. Enter `filter d/Engineer`.<br>
   Expected: The list is empty and HuntR reports `0 employees listed!`, because only whole words match.
5. Enter `filter id/e0004`.<br>
   Expected: Only `David Li` is displayed. The employee ID is not case-sensitive.
6. Enter `filter d/Engineering`, followed by `filter d/Sales`.<br>
   Expected: Only `Roy Balakrishnan` is displayed. The second filter replaces the first instead of narrowing it.
7. Enter `filter`, `filter Engineering`, `filter x/foo` and `filter d/@@@`, one at a time.<br>
   Expected: HuntR reports an invalid command format for the first two, an unknown parameter for the third, and that
   the value of `d/` needs at least one letter or digit for the last. Each message is followed by the `filter` usage.
   The displayed list does not change.
8. Enter `list`.<br>
   Expected: All six employees are displayed again.

### Sorting employees

**Prerequisites:** Launch HuntR with the original sample employees, then enter `list`. The employees are displayed from
`E0001` to `E0006`.

1. Enter `sort name desc`.<br>
   Expected: HuntR reports `Sorted by name in descending order.` The employees are displayed from `Roy Balakrishnan`
   to `Alex Yeoh`.
2. Enter `sort department`.<br>
   Expected: HuntR reports `Sorted by department in ascending order.` The department order is Engineering, Finance,
   Human Resources, Operations and Sales. The two Engineering employees are in the order `E0001`, `E0004`.
3. Enter `sort Department DESC`.<br>
   Expected: The field and the order are not case-sensitive. The department order is reversed, but the two Engineering
   employees are still in the order `E0001`, `E0004`.
4. Enter `sort name desc`, followed by `filter d/Engineering`.<br>
   Expected: `David Li` is displayed before `Alex Yeoh`. The sorted order is kept by the filter.
5. Enter `add id/E0007 n/Zed Zhang p/91234567 e/zed@example.com d/Sales r/Analyst`.<br>
   Expected: The new employee appears at the position that fits the sorted order, and the full list is displayed.
6. Enter `sort salary`, `sort name down`, `sort` and `sort name asc desc`, one at a time.<br>
   Expected: HuntR reports an unknown sort field, an unknown sort order, and an invalid command format twice. Each message
   is followed by the `sort` usage. The displayed order does not change.
7. Enter `list`.<br>
   Expected: HuntR displays all employees in the order in which they were added, from `E0001` to `E0007`.

### Archiving and restoring employees

**Prerequisites:** Start with an empty roster (no active or archived employees) in a separate test data file.
Add these active employees in the order shown, with no managers, and restore that starting state before each
independent test below. `A9999` must not exist.

```text
add id/A1001 n/Alice Tan p/91234561 e/alice.tan@example.com d/Engineering r/Engineer
add id/A1002 n/Alice Lim p/91234562 e/alice.lim@example.com d/Engineering r/Manager
add id/A1003 n/Bob Ng p/91234563 e/bob.ng@example.com d/Sales r/Engineer
```

1. Enter `archive id/a1001`, then `list archived`.<br>
   Expected: The archive result begins with `Archived employee:` and uses stored ID `A1001`. Only `A1001` appears
   in the archive view, with an `Archived` label. Its other details are unchanged.
2. Archive `A1001`, then enter `list`.<br>
   Expected: Only the active employees `A1002` and `A1003` appear.
3. Archive `A1001`, then enter `find Alice`.<br>
   Expected: Only `A1002` appears; the archived employee `A1001` is excluded.
4. Archive `A1001`, then enter `filter d/Engineering`.<br>
   Expected: Only `A1002` appears; the archived employee `A1001` is excluded.
5. Archive `A1001`, then enter `stats`.<br>
   Expected: The summary begins with `Total active employees: 2`. Each department has one employee, and each
   role has one employee; the archived employee `A1001` is excluded from all counts.
6. Archive `A1001`, then enter
   `add id/a1001 n/Carol Lee p/91234564 e/carol.lee@example.com d/Finance r/Analyst`.<br>
   Expected: The duplicate ID is rejected even with different letter case; no new employee is added.
7. Enter `find Alice`, `sort id desc`, `archive id/A1001`, then `unarchive id/a1001`.<br>
   Expected: Archiving leaves `A1002` visible. Restoration displays `A1002`, then `A1001`, without a `list` hint.
   Repeat from the starting state with `filter d/Engineering r/Engineer`:<br>
   Expected: Archiving leaves an empty view; unarchiving brings `A1001` back into that same filter.
8. Enter `find Nobody`, `archive id/A1001`, then `unarchive id/A1001`.<br>
   Expected: Both commands find the hidden employee. The view stays empty, and restoration appends
   ``The restored employee is outside the current view. Use `list` to see it.`` Enter `list` to see all three records.
9. Archive `A1001` and `A1002`, then enter `list archived` and `sort id desc`. Unarchive `A1001`, then `A1002`.<br>
   Expected: The first restoration leaves `A1002` visible; the second leaves an empty archive view. Both suggest
   `list`. Enter `undo`:<br>
   Expected: `A1002` reappears as archived. Enter `redo`:<br>
   Expected: It disappears again. Enter `list` to see all three active employees.
10. Enter `archive id/A1001`, `list archived`, then `undo`.<br>
   Expected: The message is `Undo successful.` and the archive view becomes empty. Enter `list`:<br>
   Expected: All three employees appear as active, including the restored employee `A1001`.
11. Enter `manager id/A1001 m/A1002`, `manager id/A1003 m/A1002`, `archive id/A1003`, then `archive id/a1002`.<br>
   Expected: The manager's result adds `1 active employee still reports to this archived manager.` and
   `Use team id/A1002 to review their assignments.` Enter `team id/A1002`:<br>
   Expected: Both reports retain manager `A1002`; the cards include the archived manager and archived report.
   Unarchive `A1002`, then undo and redo:<br>
   Expected: Every manager assignment remains unchanged throughout.
12. Enter `manager id/A1001 m/A1002`, `manager id/A1003 m/A1002`, then `archive id/a1002`.<br>
   Expected: The manager's result adds `2 active employees still report to this archived manager.` and
   `Use team id/A1002 to review their assignments.` Enter `team id/A1002`:<br>
   Expected: The manager is archived; both reports remain active and retain manager `A1002`.
   Unarchive `A1002`, then undo and redo:<br>
   Expected: Every manager assignment remains unchanged throughout.
13. Enter `archive`, `archive id/`, `archive id/A1001 id/A1001`, `archive x/A1001`, `archive extra id/A1001`
   and `archive id/A 1001` separately; repeat each malformed form with `unarchive`.<br>
   Expected: Each reports the corresponding input error and command-specific usage. Data and view are unchanged.
   Enter `archive id/A9999` and `unarchive id/A9999`:<br>
   Expected: `No employee with ID A9999 was found.` without usage.
14. Archive `A1001`, unarchive it, then undo. Attempt `archive id/A1001` and `unarchive id/A1002`.<br>
   Expected: The errors say that `A1001` is already archived and `A1002` is already active. Enter `redo`:<br>
   Expected: `A1001` becomes active; neither failed request consumed the redo step.
15. Enter `list archived` with no archived employees.<br>
   Expected: An empty panel and `There are no archived employees.` Archive `A1002`, then `A1001`, and enter
   `list archived`:<br>
   Expected: `A1001`, then `A1002`, in stored roster order. After `sort id desc`, `list archived` resets that order.
   `list Archived`, `list all` and `list archived id/A1001` report invalid format and retain the previous view.
   Archive `A1003` and enter `list`:<br>
   Expected: ``There are no active employees. Use `list archived` to see the 3 archived employees.``
16. Archive `A1001`, exit and relaunch, then enter `list archived`.<br>
   Expected: `A1001` is still archived with the same details. Unarchive it, exit and relaunch again:<br>
   Expected: `A1001` is active. `undo` immediately after either restart reports that no commands are available to undo.

### Deleting an employee

**Prerequisites:** Add employees with IDs `EMP-0042`, `E0123` and `abc`. Give `EMP-0042` and `E0123` the same name, and give `abc` a different name. Restore these records before each independent test case below.

1. Enter `delete id/EMP-0042`.<br>
   Expected: Only `EMP-0042` is deleted. HuntR displays `Deleted employee:` followed by all its details. `E0123`, which has the same name, remains.
2. Enter `delete id/e0123`.<br>
   Expected: The employee stored as `E0123` is deleted, and the success message uses the stored ID `E0123`. Employee IDs are matched case-insensitively. Repeat with `delete id/abc` to verify an all-letter ID.
3. Enter `find` followed by the name of employee `abc`, then enter `delete id/EMP-0042`.<br>
   Expected: The hidden employee `EMP-0042` is deleted from the complete roster while the current search result remains displayed. Enter `list` to confirm that `EMP-0042` is gone. Repeat after a search that displays zero employees.
4. Enter each of `delete`, `delete id/`, `delete x/value`, `delete id/abc!`, `delete id/abc id/abc` and `delete extra id/abc` separately.<br>
   Expected: HuntR respectively reports a missing ID, a blank ID, an unknown parameter, an invalid ID, a repeated `id/` field, or unexpected arguments, together with the `delete` usage. No record or active filter is changed.
5. Enter `delete id/E9999`.<br>
   Expected: HuntR displays `No employee with ID E9999 was found.` No usage text is appended and no record is changed.
6. Delete the last remaining employee, enter `exit`, and relaunch HuntR.<br>
   Expected: The roster remains empty after relaunch. The deleted ID can be used again in a new `add` command.
7. Enter `archive id/EMP-0042`, `list archived`, then `delete id/emp-0042`.<br>
   Expected: The archived employee is deleted, and the archived-only view remains selected. Enter `undo`:<br>
   Expected: The employee reappears with its original ID spelling and archived status. Enter `redo` to delete it again.
8. Enter `manager id/E0123 m/EMP-0042` and `manager id/abc m/EMP-0042`, then archive `abc`.
   Enter `delete id/EMP-0042`.<br>
   Expected: Both reports remain, with no manager; `abc` remains archived. The result includes
   `2 direct reports now have no manager.` Enter `undo`, then `team id/EMP-0042`:<br>
   Expected: The manager and both reports appear with their original assignments. Enter `redo`:<br>
   Expected: The manager is deleted again and both assignments are cleared.
9. Enter `filter id/E0123`, `sort id desc`, then `delete id/EMP-0042`.<br>
   Expected: `E0123` stays visible and the filter and descending ID sort remain selected. Enter `delete id/E0123`:<br>
   Expected: The view becomes empty. Enter `undo` to restore `E0123` into the same filtered view.


### Reporting relationships and team structure

**Prerequisites:** Use a build containing the reporting implementation from PR #110. Add active employees
`R1001`, `R1002` and `R1003`, initially without managers. Ensure `R9999` does not exist. Back up the data before
testing deletion or editing the JSON file.

1. Enter `manager id/r1002 m/r1001`, then `manager id/R1003 m/R1002`.<br>
   Expected: The assignments succeed using the stored ID spelling. The cards show managers `R1001` and `R1002`.
2. Enter `team id/R1002`, `org id/R1001` and `chain id/R1003` separately.<br>
   Expected: The team contains all three employees and reports one direct report. The organisation query excludes
   `R1001` and lists `R1002` then `R1003` in its message. The chain message lists `R1002` then `R1001`.
3. Enter `manager id/R1001 m/R1003`, `manager id/R1001 m/R1001`, `manager id/R1002 m/R9999` and
   `manager id/R9999 m/R1001` separately.<br>
   Expected: Respectively, HuntR reports `Reporting relationships cannot contain a cycle.`,
   `An employee cannot report to themselves.`, `Manager does not exist: R9999` and
   `No employee with ID R9999 was found.` The assignments and current view remain unchanged.
4. Enter `team id/R1002 id/R1003`, `team id/R1002 x/foo` and `manager id/R1002 m/` separately.<br>
   Expected: Each command reports an input error with its own usage and leaves records and the view unchanged.
5. Enter `edit id/R1003 r/Senior Analyst`, then `unassign-manager id/R1003` twice.<br>
   Expected: The role edit preserves manager `R1002`; both unassign commands succeed, leaving the manager unset.
   Enter `edit id/R1003 m/R1002` to restore it, then `edit id/R1003 m/` to verify removal through editing.
6. Restore `R1003`'s manager to `R1002`, archive `R1003`, then enter `team id/R1002`.<br>
   Expected: The message counts the archived direct report and the cards include `R1003`. Enter `list archived`
   to confirm that its manager assignment remains. Delete `R1002`, then enter `list archived` again.<br>
   Expected: `R1003` remains archived with no manager; the delete message reports one affected direct report.
7. Assign a manager to a remaining employee, exit and relaunch.<br>
   Expected: The assignment is retained. With HuntR closed, remove `managerId` from a record in a copy of the
   valid JSON file and load it: that employee has no manager. A missing manager reference, self-reference or
   reporting cycle in the file causes the entire file to be rejected. Restore the backup after testing.

### Undoing and redoing employee changes

Use disposable writable folders and the release JAR `huntr.jar`. Run each scenario in order. A restart preserves
saved records but resets session history. These tests complement the command syntax and examples in the UG.

**Navigation, no-ops and redo branching**

**Prerequisites:** Start a new session with the default history setting. ID `HIST-001` must be unused in both rosters.

1. Enter `undo`, then `redo`.<br>
   Expected: `No more commands to undo.` and `No more commands to redo.` respectively. No data or view changes.
2. Enter `add id/HIST-001 n/History Demo p/91234567 e/history@example.com d/QA r/Tester`, then
   `edit id/HIST-001 r/Analyst`, then `undo`.<br>
   Expected: The role changes from Analyst back to Tester; the result is `Undo successful.`
3. Enter `edit id/HIST-001 r/Tester`, then `edit id/HIST-001 p/12`, then `find History`.<br>
   Expected: The first edit reports unchanged details; the second reports the invalid phone number. Neither
   consumes a history step or discards redo. The search displays History Demo.
4. Enter `redo`, then `undo`, then `edit id/HIST-001 r/Lead`, then `redo`.<br>
   Expected: Redo restores Analyst, undo restores Tester, and the new edit changes the role to Lead.
   The final redo reports `No more commands to redo.` because the actual new change discarded the redo state.

**Archived view and restart**

**Prerequisites:** Continue from the preceding scenario with `HIST-001` active and its role Lead.

1. Enter `archive id/HIST-001`, then `list archived`, then `undo`.<br>
   Expected: Undo restores `HIST-001` to active status, but the archived-only view remains selected and hides it.
2. Enter `redo`.<br>
   Expected: The archived-only view is preserved and displays `HIST-001` again, with role Lead.
3. Exit and relaunch without changing the data file. Enter `undo`, then `redo`, then `list archived`.<br>
   Expected: Both history commands report that no more commands are available. `HIST-001` is still archived;
   only its saved employee state, not its history, survived the restart.

**Finite capacity and eviction**

**Prerequisites:** Put a copy of the JAR in a fresh writable folder. Launch with
`java -Dhuntr.maxUndoHistory=2 -jar huntr.jar`. Keep this session open for the save-failure scenario below.

1. Enter `add id/LIM-001 n/Limit Demo p/91234567 e/limit@example.com d/QA r/Tester`, then
   `edit id/LIM-001 r/Analyst`, then `edit id/LIM-001 r/Lead`.<br>
   Expected: All three changes succeed. Only the last two changes remain undoable.
2. Enter `undo` twice, then a third time.<br>
   Expected: The role becomes Analyst, then Tester. The third undo reports `No more commands to undo.`;
   `LIM-001` still exists because the pre-add snapshot was evicted.
3. Enter `redo` twice.<br>
   Expected: The role becomes Analyst, then Lead. A further redo reports `No more commands to redo.`

**Save failure restores evicted history and redo**

**Prerequisites:** Continue the finite-capacity scenario at Lead, with two available undo steps. On macOS/Linux,
copy `data/addressbook.json` to `before-save.json`, then run `chmod a-w data/addressbook.json` in the launch folder.
Use this permission test only on a disposable copy of the data.

1. Enter `edit id/LIM-001 r/Director`.<br>
   Expected: A save-permission error is shown. The role stays Lead, despite the tentative commit having reached
   capacity. `cmp data/addressbook.json before-save.json` reports no differences.
2. Run `chmod u+w data/addressbook.json`, then enter `undo` twice.<br>
   Expected: Analyst and then Tester are restored. The failed edit did not permanently evict the oldest state.
3. With the role still Tester, run `chmod a-w data/addressbook.json` and enter `redo`.<br>
   Expected: A save-permission error is shown and the role stays Tester. Restore permission with
   `chmod u+w data/addressbook.json`, then enter `redo` twice: Analyst and Lead are both still recoverable.

**Disabled history and invalid configuration**

1. Close the preceding session and relaunch with `java -Dhuntr.maxUndoHistory=0 -jar huntr.jar`.
   Enter `edit id/LIM-001 r/Director`, then `undo`, then `redo`.<br>
   Expected: The edit succeeds and is saved; undo and redo both report that no more commands are available.
   Relaunch normally to check that the role is Director and that history starts empty again.
2. Close HuntR and try `java -Dhuntr.maxUndoHistory=-2 -jar huntr.jar`, then
   `java -Dhuntr.maxUndoHistory=invalid -jar huntr.jar`.<br>
   Expected: Both launches fail because the configured history limit is invalid; neither changes the saved roster.

### Saving data

The employee data file is `data/addressbook.json`, relative to the folder from which `java -jar huntr.jar` is run. Back up this file before the following tests and restore it afterwards.

**Persisting a successful change**

**Prerequisites:** Launch HuntR and ensure employee ID `SAVE-001` does not exist.

1. Enter `add id/SAVE-001 n/Save Test p/91234567 e/save@example.com d/QA r/Tester`.<br>
   Expected: HuntR reports that the employee was added and updates `data/addressbook.json` without requiring a separate save command.
2. Enter `exit`, then run `java -jar huntr.jar` again.<br>
   Expected: Employee `SAVE-001` is loaded with all six fields unchanged.
3. Enter `delete id/SAVE-001`, exit, and relaunch HuntR.<br>
   Expected: Employee `SAVE-001` remains deleted after relaunch.

**Dealing with a missing data file**

**Prerequisites:** Close HuntR. Move `data/addressbook.json` to a backup location so that the original path does not exist.

1. Run `java -jar huntr.jar`.<br>
   Expected: HuntR starts with the six sample employees instead of failing to launch.
2. Enter `list`, then check that `data/addressbook.json` does not exist yet. Enter `add id/E0007 n/Test User p/91234567 e/test@example.com d/QA r/Tester` and close HuntR.<br>
   Expected: `list` does not create the file, because it does not change data. After the `add`, HuntR creates a new `data/addressbook.json` containing the six sample employees and `Test User`. Restore the backup after closing the app if the original data is still needed.

**Dealing with a corrupted data file**

**Prerequisites:** Close HuntR and back up `data/addressbook.json`. Replace its contents with text that is not valid JSON, such as `{ invalid json`.

1. Run `java -jar huntr.jar`.<br>
   Expected: HuntR starts with an empty employee list. It does not load sample employees for a corrupted file. The result box shows a warning that `data/addressbook.json` could not be loaded and that a copy was kept at `data/unreadable-addressbook.json`, and the log records a warning that the data file could not be loaded. The copy contains `{ invalid json`.
2. Enter `list`, then `add id/E0001 n/Test User p/91234567 e/test@example.com d/QA r/Tester`.<br>
   Expected: HuntR remains usable and `list` displays the empty-list guidance, without changing `data/addressbook.json`, which still contains the corrupted text. After the `add`, HuntR replaces the corrupted file with valid JSON containing only `Test User`. `data/unreadable-addressbook.json` still contains the corrupted text. Close HuntR and restore the backup after the test.

**Dealing with invalid employee data**

**Prerequisites:** Close HuntR and back up `data/addressbook.json`. Edit one stored employee so that its ID contains an invalid character, or duplicate an employee using the same ID with different letter case.

1. Run `java -jar huntr.jar`.<br>
   Expected: HuntR rejects the entire file and starts with an empty employee list; it does not partially load valid employees. The result box shows the warning with the location of `data/unreadable-addressbook.json`, which is a copy of the rejected file, and the log records a warning describing the invalid data.
2. Close HuntR without entering a command and restore the backup.<br>
   Expected: The original employee data is available again on the next launch.
