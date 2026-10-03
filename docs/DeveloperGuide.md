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

*{More to be added}*

### Use cases

(For all use cases below, the **System** is the `AddressBook` and the **Actor** is the `user`, unless specified otherwise)

**Use case: Delete a person**

**MSS**

1.  User requests to list persons
2.  AddressBook shows a list of persons
3.  User requests to delete a specific person in the list
4.  AddressBook deletes the person

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. AddressBook shows an error message.

      Use case resumes at step 2.

*{More to be added}*

### Non-Functional Requirements

The following requirements describe the intended HuntR product, including capabilities that are not yet implemented. Performance limits are acceptance targets, not measured results for the current version. Platform, packaging, storage, and display requirements incorporate the applicable [course project constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).

HuntR is intended for one HR administrator managing workforce records locally. Shared access to the same data by different users, including taking turns on a shared installation, is outside the supported usage.

1. **NFR-01: Platform compatibility.** HuntR must launch and support its employee-management commands on Windows, Linux, and macOS with Java `25` installed, without requiring another Java version. Verify the same release on each operating system by adding, listing, finding, and deleting an employee, then exiting and relaunching.

2. **NFR-02: Portable distribution.** HuntR must be distributed as a single executable JAR containing its application dependencies and resources. With Java `25` already installed, a user must be able to copy the JAR into a writable folder and launch it using `java -jar addressbook.jar`, without an installer or separate library installation.

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
| **Employee ID** | The unique identifier for an employee record: uppercase `E` followed by exactly four digits, such as `E0123`. It is supplied using `id/` and distinguishes employees even when their names are identical. |
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

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
