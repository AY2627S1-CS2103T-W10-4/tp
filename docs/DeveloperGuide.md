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
1. Logic saves the employee state and returns the `CommandResult` only after saving succeeds. Data-changing commands use the checkpoint and rollback boundary described below.

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

The writer resolves the destination and inspects its supported access attributes, creates missing parent
directories, then creates a uniquely named sibling `.huntr-*.tmp` file. It establishes and verifies restricted
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

`LogicManager` continues saving after every successfully executed command. Its generic presentation is
`Could not save data due to the following error: <exception-message>`. Its permission presentation is
`Could not save data to file <exception-message> due to insufficient permissions to write to the file or the folder.`
The latter substitutes `AccessDeniedException.getMessage()`, including any paths and additional diagnostic text.
`BackupCommand` translates every `IOException` into
`Could not save the backup to <backup-path>. Check that you are allowed to write to that folder.`.
If backup creation succeeds but the automatic save fails, the backup remains and the automatic-save error is the
final result, rather than the backup success message. For data-changing commands, including undo and redo,
`LogicManager` restores the checkpoint on save failure, preserving employee records, the complete history and the
effective view as well as the stored file. Read-only command view changes and completed backup/export files remain.

The pretty-printed UTF-8 schema remains an `employees` array containing `id`, `name`, `phone`, `email`, `department`,
`role` and `archived`. Employee order, raw values and archive status are preserved. Missing `archived` loads as active;
empty arrays are valid; invalid employee data and missing/null arrays are rejected; unknown properties are ignored.
No history, transaction or schema-version fields are added.

The package-private `AtomicFileWriter.FileOperations` seam delegates to ordinary JDK operations. It exposes only
temporary creation, attribute views/writability, a write/force/close handle, atomic replacement and cleanup.
Writer tests inject faults there; storage tests verify serialization ordering and payloads; focused logic/backup
tests verify presentation. Package-private parser/storage factory hooks isolate backup test paths without changing
public constructors or command behavior. See the repository's `tests/test-plan.md` for coverage and release gates.

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
`ReportingCommandSupport.show()` installs an ID-membership predicate without resetting the `SortedList` comparator.
Employee cards therefore preserve the current sort order, or roster order when no sort is active.
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

`EmployeeCard` and `EmployeeListCard.fxml` display the manager ID or `Unassigned`. Reporting usage is also listed
by `HelpCommand`. `ReportingCommandTest`, `ReportingCommandParserTest` and `ReportingStorageTest` cover command
behaviour, strict input validation and persistence; manual scenarios are provided below.

### Workforce statistics

`StatsCommand` reads the complete roster through `Model#getAddressBook()` and
`ReadOnlyAddressBook#getEmployeeList()`, then excludes archived employees. It reports the active employee count
and calls `appendBreakdown` for department and role. Each breakdown groups by the stored field value and sorts
by count descending, with ties ordered by the stored label ascending. An empty active roster shows a total of
zero and `(none)` under each heading.

The sequence diagram below focuses on `StatsCommand#execute` after parsing. The command does not modify employee
records, the current filter, the sort order or undo/redo history. As with other successful commands,
`LogicManager` still saves the employee state before returning the result to the UI; that shared saving flow
is outside this diagram.

<puml src="diagrams/StatsSequenceDiagram.puml" alt="Interactions for summarising the active workforce" />

### Employee archiving

`Employee` has an immutable boolean archive status. Active and archived employees remain in the same
`UniqueEmployeeList`, so case-insensitive ID uniqueness, editing and deletion naturally apply regardless of status.
`archive id/EMPLOYEE_ID` and `unarchive id/EMPLOYEE_ID` replace the target with an otherwise identical employee whose
status is changed. They reject unknown IDs and redundant status changes without modifying the model.

`ModelManager#updateFilteredEmployeeList` combines normal predicates with an active-status check, so `list` and `find`
never expose archived employees. `list archived` calls `Model#showArchivedEmployees` to select only archived employees
in the same existing employee panel. `edit` preserves the target's archive status, while `delete` searches the complete
stored list and can remove either status; session history can restore the removed record.

`JsonAdaptedEmployee` persists the status as `archived` in each employee object. A missing field deserializes as `false`,
preserving compatibility with legacy data. `ClearCommand` calls `Model#clearEmployees`, which removes only active
employees and preserves archived entries in the same list.

### Delete an employee by ID

`DeleteCommandParser` accepts `delete id/EMPLOYEE_ID` with exactly one `id/` parameter. It distinguishes unknown parameters, a missing parameter, a blank ID, an invalid ID format and duplicate parameters. A preamble before `id/` is rejected as unexpected arguments. Parsing errors include the command usage; see the [User Guide](UserGuide.md#deleting-an-employee-delete) for messages and examples.

ID validation uses `ParserUtil.parseEmployeeId` and the existing `EmployeeId` rules: 1 to 20 letters, digits, hyphens or underscores, with no spaces. IDs retain their original casing and compare case-insensitively.

`DeleteCommand` searches `model.getAddressBook().getEmployeeList()` for the matching `EmployeeId`, so a target can be deleted even when hidden by the current filter. It deletes that stored employee and returns all their details. The filter and remaining roster order are preserved. An absent ID produces `No employee with ID X was found.` without usage, where `X` is the entered ID. This command uses the existing saving flow.

### Edit an employee by ID

`EditCommandParser` requires exactly one `id/` parameter and at least one field: `n/`, `p/`, `e/`, `d/` or `r/`;
the reporting implementation also accepts `m/` as described above. It rejects unknown parameters, a non-empty
preamble and repeated prefixes. A missing ID or unexpected preamble produces `Invalid command format.` followed
by usage; an ID without any fields produces `At least one field to edit must be provided.`

`ParserUtil` validates the ID and supplied field values, returning the corresponding validation messages for invalid values. The parser stores validated changes in an `EditEmployeeDescriptor`; omitted fields keep their existing values. The ID only selects the employee and cannot be changed.

`EditCommand` searches `model.getAddressBook().getEmployeeList()` using case-insensitive `EmployeeId` equality, so filtered-out employees can be edited. It creates a replacement employee with the stored ID, including its original casing, and calls `model.setEmployee`. It then calls `updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES)` to show the full roster and returns `Edited employee: ` followed by the updated details. An unknown ID produces `No employee with ID X was found.` without usage, where `X` is the entered ID. Successful edits use the existing saving flow.

The sequence diagram below shows a successful `edit id/E0001 r/Manager`, adapted from the delete sequence diagram in the Logic component section. It focuses on parsing and model updates; `LogicManager` saves the address book before returning the result, as usual.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions for editing an employee by ID" />

### Find employees by name

`FindCommandParser` trims the arguments and splits them into keywords on whitespace using `split("\\s+")`. Empty or whitespace-only arguments, and arguments beginning with `/`, produce `Invalid command format.` followed by the command usage. Each keyword is then checked against the existing `Name` validation rules; an invalid keyword produces the keyword validation error. Otherwise, the parser creates a `NameContainsKeywordsPredicate` and passes it to `FindCommand`.

`NameContainsKeywordsPredicate` uses `StringUtil.containsWordIgnoreCase` to match any keyword against a full, whitespace-separated word in the employee's name. Matching is case-insensitive and uses OR semantics: `find alex david` matches either name, while `find al` does not match `Alex`. It does not search employee IDs or other fields.

`FindCommand` calls `model.updateFilteredEmployeeList(predicate)` to replace the current filter over the full employee list, without changing employee records. It returns the filtered list's size through `Messages.getEmployeesListedOverview`, using `1 employee listed!` or `N employees listed!`. No matches is a successful search returning `0 employees listed!`.

### Duplicate employee detection

`Employee#isSameEmployee` compares employee IDs only, using case-insensitive `EmployeeId` equality. Employees with IDs `E0001` and `e0001` are duplicates even if their other details differ. Matching names or contact details with different IDs do not make employees duplicates; `Employee#equals` separately compares all fields.

`UniqueEmployeeList` enforces this identity rule for all employees regardless of archive status. An operation that would
introduce duplicate IDs throws `DuplicateEmployeeException`. `AddCommand` first calls `model.hasEmployee(toAdd)`, which
therefore includes archived employees. A duplicate produces `An employee with Employee ID X already exists.`, where `X` is
the supplied ID, without adding the employee. Otherwise, it calls `model.addEmployee`; a same or similar name only adds a
warning to the success message.

When loading JSON, `JsonSerializableAddressBook#toModelType` converts and validates every employee. Duplicate IDs,
including IDs differing only in case, cause an `IllegalValueException`, rejecting the file rather than returning
partially loaded data.

### CSV import and export

`ImportCommand` and `ExportCommand` extend the abstract `CsvFileCommand`, which holds the file path and the folder
HuntR runs from. `ParserUtil.parseCsvFilePath` checks that the path is relative and ends in `.csv`, and
`CsvFileCommand#resolveFilePath` rejects a path that leads outside that folder once symbolic links are followed.
`CsvUtil` converts between a list of values and one CSV line, quoting values that contain commas or quotes.

`ExportCommand` writes the header in `ExportCommand.CSV_HEADER`, then one line per employee in the complete stored
roster, including archived employees. It creates missing parent folders and replaces an existing file.

The sequence diagram below shows a successful `import staff.csv`. `ImportCommand` reads every line, checks the
header against the three accepted column lists, and parses each non-blank row with the same `ParserUtil` methods as
`add`. A row that is invalid, repeats an earlier ID or uses an existing ID stops the command with its row number.
`ReportingStructure.validate` then checks the managers of the existing and imported employees together. Only after
every check passes does the command change the model, so a failed import adds no one. It adds every employee without
a manager first and then sets the managers, so a manager may appear later in the file than their reports.

<puml src="diagrams/ImportSequenceDiagram.puml" alt="Interactions for importing employees from a CSV file" />

### Employee history and in-memory rollback

`ModelManager` owns a `VersionedAddressBook`, which extends `AddressBook` and inherits its ordinary equality and
hash-code behavior. `ModelManager` equality also remains unchanged: history is not compared.
Construction copies the supplied data and records exactly one initial snapshot with pointer zero.
History has no explicit capacity limit and lasts only for the session.

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
Identical edits and clears with no active employees still reset to the active view and return their usual success
messages. They create no entry and retain redo. Read-only and failed commands also retain redo.

#### Checkpoints and transaction boundary

`Command#getHistoryAction()` defaults to `NONE`; modifying commands return `COMMIT`, and undo/redo return `NAVIGATE`.
`Command#isDataChanging()` is final and derives its result from that declaration. It means “this command may change
employee data or navigate employee history.” The nine modifying commands and both history commands therefore
return true even for no-ops. `LogicManager` parses first, captures a `ModelCheckpoint` for those commands, executes,
commits only for `COMMIT`, then saves through the existing storage API. `NAVIGATE` never creates a snapshot.
Only a successful storage return permits the result to be returned. No fallible work is added after that return.

<puml src="diagrams/CommandTransaction.puml" alt="Employee command execution, saving and rollback" />

A checkpoint copies the current employee-list container and the history container, retaining references to
immutable snapshots, the pointer and the effective predicate. Capture takes O(n + h) work for n current employees
and h snapshots; it does not reconstruct historical address books or revalidate employee pairs. Checkpoints remain
usable after later changes, after redo-tail removal and after repeated restoration. Restoration copies back into
the working containers and never changes a shared snapshot.

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
employee restored by undo. Historical searches are not part of snapshots. Selection, scroll position and
suppression of intermediate list notifications are outside scope.

#### Persistence and atomic saving

All successfully executed commands still save, including read-only commands and no-ops. Read-only save failures
retain existing behavior and are not checkpointed. Export/backup side effects are outside the transaction.
Only the current employee state is serialized in the existing `employees` JSON schema; there are no history,
checkpoint, command or predicate fields. Missing `archived` fields in older valid JSON still load as active;
validation, case-insensitive ID uniqueness, invalid-file handling, paths and preferences are unchanged. CSV export
uses eight columns including manager IDs; import also accepts legacy six-column and seven-column CSV and rejects
header-only files.

Employee history and atomic saving share the recorded baseline `1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`.
The combined boundary is the writer's successful atomic replacement. Employee changes and history commits are
tentative until then; a handled failure before replacement preserves file bytes and restores the checkpoint for
data-changing commands. No fallible work follows replacement, so a completed disk commit cannot be reported as a
failed save with memory rolled back. A failed no-op also restores its previous view and redo states. A failed new
change after undo restores the original redo sequence.

Restart loads the last successfully saved state as one initial snapshot with no available undo or redo.
External JSON editing while closed remains supported; live reload and concurrent writers are out of scope.

#### Validation and performance

`ModelHistoryTest`, `HistoryCommandTest` and `HistoryTransactionTest` cover history traversal, no-ops, raw IDs,
checkpoints, view/list identity, import grouping, execution failures and save failures. Assertions compare ordered
stored values separately from model equality. Existing parser, storage, CSV and command tests cover compatibility.
The detailed matrix and recorded acceptance environment are in the repository's `tests/test-plan.md`.

For history performance, use the hardware conditions in [Non-functional requirements](#non-functional-requirements).
Start each of three measured sessions from the same 1,000-employee fixture. Perform 100 genuine edits while retaining
exactly 1,000 employees, then time all 100 undos and all 100 redos: 600 measured history operations in total. Measure
from command dispatch through saving to the first rendered UI frame showing the completed result and list update.
Every operation must complete within two seconds. Record the maximum and environment; keep thresholds out of CI
unit tests. Run the standalone UI acceptance harness described in `tests/test-plan.md` with atomic saving enabled.

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
| `* * *` | Basic user | Delete an employee's record | my workforce records remain current |
| `* * *` | Basic user | Find an employee by name | quickly retrieve their information without browsing the whole list |
| `* * *` | Basic user | Have my data saved automatically after every change | I don't lose records if the app closes unexpectedly |
| `* * *` | Basic user | Exit the application with a command | close it safely knowing my data is saved |
| `* *` | Basic user | Edit an employee's details | keep records accurate when someone's role or contact info changes |
| `* *` | Careful user | Be asked to confirm before a record is deleted | avoid losing a record to a mistyped command |
| `* *` | Busy user | Filter employees using multiple criteria at once (e.g. department and role) | narrow down results faster than one field at a time |
| `* *` | Busy user | View an entire team's roster with one command | prepare for a team meeting without assembling the list myself |
| `*` | Busy user | Sort employees by a chosen field (e.g. name or department) | scan records in the order that's useful to me |
| `*` | Busy user | Look up an employee's leave status with one command | check quickly if they're available for a meeting |
| `*` | Long-time user | Identify employees who don't appear connected to anyone else in the organisation | investigate whether my workforce information is incomplete |
| `*` | Long-time user | Count employees by department | quickly see the size of each team |

### Use cases

(For all use cases below, the **System** is `HuntR` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Add an employee**

**MSS**

1. User requests to add an employee and provides the employee ID, name, phone number, email, department, and role.
2. HuntR validates the provided details.
3. HuntR adds the employee and saves the updated employee records.
4. HuntR displays the added employee's details.

   Use case ends.

**Extensions**

* 1a. A required field is missing.

  * 1a1. HuntR shows an error message and the expected command format.

    Use case resumes at step 1.

* 2a. One or more provided fields are invalid.

  * 2a1. HuntR shows an error message describing the invalid input.

    Use case resumes at step 1.

* 2b. An employee with the same employee ID already exists.

  * 2b1. HuntR informs the user that an employee with the same employee ID already exists.

    Use case ends.

* 3a. HuntR is unable to save the updated employee records.

  * 3a1. HuntR informs the user that the employee could not be saved successfully.

    Use case ends.

**Use case: Edit an employee**

**MSS**

1. User requests to list employees.
2. HuntR displays the employee list.
3. User selects an employee and provides the details to update.
4. HuntR validates the provided details.
5. HuntR updates the selected employee's record and saves the updated employee records.
6. HuntR displays the updated employee's details.

   Use case ends.

**Extensions**

* 2a. The employee list is empty.

  * 2a1. HuntR informs the user that there are no employee records.

    Use case ends.

* 3a. The selected employee index is invalid.

  * 3a1. HuntR shows an error message.

    Use case resumes at step 2.

* 3b. The user does not provide any field to update.

  * 3b1. HuntR informs the user that at least one field must be provided.

    Use case resumes at step 3.

* 4a. One or more provided fields are invalid.

  * 4a1. HuntR shows an error message describing the invalid input.

    Use case resumes at step 3.

* 4b. The update would make the employee identical to another existing employee.

  * 4b1. HuntR informs the user that the employee already exists.

    Use case ends.

* 5a. HuntR is unable to save the updated employee records.

  * 5a1. HuntR informs the user that the employee changes could not be saved successfully.

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

**Use case: Delete an employee**

**MSS**

1. User requests to delete an employee using `delete id/EMPLOYEE_ID`.
2. HuntR validates the ID and finds the employee in the complete roster, comparing IDs case-insensitively.
3. HuntR deletes the specified employee record and saves the updated records, preserving the current search filter.
4. HuntR displays the deleted employee's details as confirmation.

    Use case ends.

**Extensions**

* 1a. The parameter is missing or unknown, the ID is blank or malformed, or the command contains repeated parameters or unexpected arguments.

  * 1a1. HuntR identifies the specific input error and shows the command usage. Records remain unchanged.

    Use case resumes at step 1.

* 2a. No employee has the specified employee ID.

  * 2a1. HuntR displays `No employee with ID X was found.`, using the entered ID for `X`. Records remain unchanged.

    Use case ends.

* 3a. The employee is hidden by the current search.

  * 3a1. HuntR deletes that employee from the complete roster while retaining the search filter.

    Use case resumes at step 4.

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

**Prerequisites:** Launch HuntR with the sample employees. The employee ID `E1001` must not already exist.

1. Enter `add id/E1001 n/Mary-Ann O'Brien p/+65 6123-4567 (HP) e/mary.ann@example.com d/Research & Development r/Software Engineer (Backend)`.<br>
   Expected: HuntR displays `New employee added:` followed by all six details. The employee appears in the list with ID `E1001`.
2. Enter `add id/e1001 n/Another Employee p/91234567 e/another@example.com d/Operations r/Analyst`.<br>
   Expected: HuntR reports that an employee with ID `e1001` already exists. No employee is added because employee IDs are compared case-insensitively.
3. Enter `add id/E1002 n/  mary-ann   o'brien  p/81234567 e/mary2@example.com d/Finance r/Analyst`.<br>
   Expected: HuntR adds `E1002`, normalises repeated spaces in the name, and displays a warning that an employee with the same or a very similar name already exists. `E1001` remains unchanged.
4. Enter `add id/E1003 n/Test User p/12 e/test@example.com d/QA r/Tester`.<br>
   Expected: HuntR reports that phone numbers must contain at least 3 digits. No employee is added.
5. Enter `add id/E1003 n/Test User p/91234567 e/test@example.com d/QA`.<br>
   Expected: HuntR reports an invalid command format and shows the `add` command usage because the required `r/` field is missing. No employee is added.

### Listing employees

**Prerequisites:** Launch HuntR with at least two employees whose names are different.

1. Enter `find Alex`.<br>
   Expected: Only employees whose names contain the whole-word keyword `Alex`, ignoring case, are displayed.
2. Enter `list`.<br>
   Expected: HuntR displays `Listed all employees.` and restores the complete employee roster.
3. Enter `clear`, followed by `list`.<br>
   Expected: After `clear`, all active employee records are removed. `list` displays an empty active roster and a message beginning with `You don’t have any employees yet.`, followed by the `add` command format. Any archived records remain available through `list archived`.

### Finding employees

**Prerequisites:** Launch HuntR with the original sample employees, including `Alex Yeoh`, `David Li` and `Charlotte Oliveiro`.

1. Enter `find alex`.<br>
   Expected: Only `Alex Yeoh` is displayed and HuntR reports `1 employee listed!`. Matching is case-insensitive.
2. Enter `find Alex David`.<br>
   Expected: `Alex Yeoh` and `David Li` are displayed and HuntR reports `2 employees listed!`. An employee matches when its name contains any one of the whole-word keywords.
3. Enter `find NotPresent`.<br>
   Expected: The employee list is empty and HuntR reports `0 employees listed!`.
4. Enter `find`, with no keyword.<br>
   Expected: HuntR reports an invalid command format and shows the `find` command usage. The previously displayed empty result remains unchanged.
5. Enter `find /Alex`.<br>
   Expected: HuntR reports an invalid command format and shows the `find` command usage because a keyword cannot begin with `/`.
6. Enter `find Alex valid!`.<br>
   Expected: HuntR reports the keyword constraints because `!` is not allowed. The employee list and stored data remain unchanged.

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
2. Enter `list` and close HuntR.<br>
   Expected: HuntR creates a new `data/addressbook.json` containing the sample employees. Restore the backup after closing the app if the original data is still needed.

**Dealing with a corrupted data file**

**Prerequisites:** Close HuntR and back up `data/addressbook.json`. Replace its contents with text that is not valid JSON, such as `{ invalid json`.

1. Run `java -jar huntr.jar`.<br>
   Expected: HuntR starts with an empty employee list. It does not load sample employees for a corrupted file, and the log records a warning that the data file could not be loaded.
2. Enter `list`.<br>
   Expected: HuntR remains usable, displays the empty-list guidance, and replaces the corrupted file with a valid JSON representation of the current empty roster. Close HuntR and restore the backup after the test.

**Dealing with invalid employee data**

**Prerequisites:** Close HuntR and back up `data/addressbook.json`. Edit one stored employee so that its ID contains an invalid character, or duplicate an employee using the same ID with different letter case.

1. Run `java -jar huntr.jar`.<br>
   Expected: HuntR rejects the entire file and starts with an empty employee list; it does not partially load valid employees. The log records a warning describing the invalid data.
2. Close HuntR without entering a command and restore the backup.<br>
   Expected: The original employee data is available again on the next launch.
