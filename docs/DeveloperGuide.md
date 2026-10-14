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

The architecture and logic sequence diagrams below retain AB3's former index-based delete examples. HuntR's current syntax and lookup behavior are described in [Delete an employee by ID](#delete-an-employee-by-id).

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Delete an employee by ID

`DeleteCommandParser` accepts `delete id/EMPLOYEE_ID` with exactly one `id/` parameter. It distinguishes unknown parameters, a missing parameter, a blank ID, an invalid ID format and duplicate parameters. A preamble before `id/` is rejected as unexpected arguments. Parsing errors include the command usage; see the [User Guide](UserGuide.md#deleting-an-employee-delete) for messages and examples.

ID validation uses `ParserUtil.parseEmployeeId` and the existing `EmployeeId` rules: 1 to 20 letters, digits, hyphens or underscores, with no spaces. IDs retain their original casing and compare case-insensitively.

`DeleteCommand` searches `model.getAddressBook().getEmployeeList()` for the matching `EmployeeId`, so a target can be deleted even when hidden by the current filter. It deletes that stored employee and returns all their details. The filter and remaining roster order are preserved. An absent ID produces `No employee with ID X was found.` without usage, where `X` is the entered ID. This command uses the existing saving flow.

### Edit an employee by ID

`EditCommandParser` requires exactly one `id/` parameter and at least one field: `n/`, `p/`, `e/`, `d/` or `r/`. It rejects unknown parameters, a non-empty preamble and repeated prefixes. A missing ID or unexpected preamble produces `Invalid command format.` followed by usage; an ID without any fields produces `At least one field to edit must be provided.`

`ParserUtil` validates the ID and supplied field values, returning the corresponding validation messages for invalid values. The parser stores validated changes in an `EditEmployeeDescriptor`; omitted fields keep their existing values. The ID only selects the employee and cannot be changed.

`EditCommand` searches `model.getAddressBook().getEmployeeList()` using case-insensitive `EmployeeId` equality, so filtered-out employees can be edited. It creates a replacement employee with the stored ID, including its original casing, and calls `model.setEmployee`. It then calls `updateFilteredEmployeeList(PREDICATE_SHOW_ALL_EMPLOYEES)` to show the full roster and returns `Edited employee: ` followed by the updated details. An unknown ID produces `No employee with ID X was found.` without usage, where `X` is the entered ID. Successful edits use the existing saving flow.

The sequence diagram below shows a successful `edit id/E0001 r/Manager`, adapted from the delete sequence diagram in the Logic component section. It focuses on parsing and model updates; `LogicManager` saves the address book before returning the result, as usual.

<puml src="diagrams/EditSequenceDiagram.puml" alt="Interactions for editing an employee by ID" />

### Find employees by name

`FindCommandParser` trims the arguments and splits them into keywords on whitespace using `split("\\s+")`. Empty or whitespace-only arguments produce `Invalid command format.` followed by the command usage. Otherwise, it creates a `NameContainsKeywordsPredicate` and passes it to `FindCommand`.

`NameContainsKeywordsPredicate` uses `StringUtil.containsWordIgnoreCase` to match any keyword against a full, whitespace-separated word in the employee's name. Matching is case-insensitive and uses OR semantics: `find alex david` matches either name, while `find al` does not match `Alex`. It does not search employee IDs or other fields.

`FindCommand` calls `model.updateFilteredEmployeeList(predicate)` to replace the current filter over the full employee list, without changing employee records. It returns the filtered list's size through `Messages.getEmployeesListedOverview`, using `1 employee listed!` or `N employees listed!`. No matches is a successful search returning `0 employees listed!`.

### Duplicate employee detection

`Employee#isSameEmployee` compares employee IDs only, using case-insensitive `EmployeeId` equality. Employees with IDs `E0001` and `e0001` are duplicates even if their other details differ. Matching names or contact details with different IDs do not make employees duplicates; `Employee#equals` separately compares all fields.

`UniqueEmployeeList` enforces this identity rule when adding employees, replacing an employee or replacing the whole list. An operation that would introduce duplicate IDs throws `DuplicateEmployeeException`. `AddCommand` first calls `model.hasEmployee(toAdd)`, which checks the full address book through `UniqueEmployeeList`. A duplicate produces `An employee with Employee ID X already exists.`, where `X` is the supplied ID, without adding the employee. Otherwise, it calls `model.addEmployee`; a same or similar name only adds a warning to the success message.

When loading JSON, `JsonSerializableAddressBook#toModelType` converts and validates each stored employee, then checks `addressBook.hasEmployee` before adding it. Duplicate IDs, including IDs differing only in case, cause an `IllegalValueException` with `Employees list contains duplicate employee(s).`, rejecting the data file rather than returning a partially loaded address book.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

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
| **Reporting relationship** | The relationship identifying which employee another employee reports to in the organisation, as referenced by the proposed reporting-structure user stories. |
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
   Expected: After `clear`, all employee records are removed. `list` displays an empty list and a message beginning with `You don’t have any employees yet.`, followed by the `add` command format.

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
