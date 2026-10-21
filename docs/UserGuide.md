---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# HuntR User Guide

HuntR is a **desktop application for a sole HR administrator to manage employee records, optimised for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you can type quickly, HuntR can help you add, find, update and remove employee records faster than traditional GUI-based HR tools.

HuntR is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) from SE-EDU. This guide covers version 1.3.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `huntr.jar` file from [here](https://github.com/AY2627S1-CS2103T-W10-4/tp/releases).

1. Copy the file to the folder you want to use as the _home folder_ for HuntR.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar huntr.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all employees.

   * `add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer` : Adds an employee named `John Tan` to HuntR.

   * `edit id/E0123 p/92345678 r/Senior Software Engineer` : Updates the phone number and role of employee `E0123`.

   * `find Alex` : Finds employees whose names contain the word `Alex`.

   * `delete id/E0123` : Deletes employee `E0123`, including when hidden by a search.

   * `clear` : Deletes all active employees while preserving archived records.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add id/EMPLOYEE_ID n/NAME`, replace `EMPLOYEE_ID` and `NAME` with values such as `E0123` and `John Doe`.

* Items in square brackets are optional.<br>
  For example, `edit id/EMPLOYEE_ID [p/PHONE]` can be used as `edit id/E0001 p/91234567`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE`, `p/PHONE n/NAME` is also acceptable.

* Extraneous parameters for `help` and `clear` are ignored.<br>
  For example, `help 123` is interpreted as `help`. The `list` and `exit` commands also take no parameters but reject extra text, so `list 1` and `exit now` show an error.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Opens a help window that lists the format of every command and links to this user guide.

![help message](images/helpMessage.png)

Format: `help`

* Click **Copy URL** to copy the link to this user guide.
* You can also open the help window by pressing <kbd>F1</kbd> or choosing **Help** in the menu bar.


### Adding an employee: `add`

Adds an employee to HuntR.

Format: `add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE`

All six fields are required. The employee ID must be 1 to 20 characters long and contain only letters, digits,
hyphens and underscores. Employee IDs are compared case-insensitively, so `E0123` and `e0123` identify the
same employee. For example, if `E0123` is already in use, HuntR displays
`An employee with Employee ID E0123 already exists.` and does not add the employee.

Parameters can be supplied in any order.

Examples:
* `add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`
* `add id/EMP-0042 n/Nur Aisyah p/87654321 e/nur.aisyah@example.com d/Human Resources r/HR Executive`
* `add id/2024-017 n/Marcus Lee p/90001111 e/marcus.lee@example.com d/Finance r/Accountant`

### Listing all employees: `list`

Shows all employees stored in HuntR as a numbered list. Each entry includes the employee's name, employee ID,
phone number, email, department, and role.

Format: `list`

The command takes no parameters. Leading and trailing spaces are ignored, but additional text is rejected.

If there are no employee records, HuntR displays an empty employee list and explains how to add an employee.

### Editing an employee: `edit`

Edits an existing employee in HuntR using their employee ID.

Format: `edit id/EMPLOYEE_ID [n/NAME] [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [r/ROLE]`

* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* The `id/` parameter identifies the employee to edit. The employee ID itself cannot be changed.
* Employee ID matching is case-insensitive: `edit id/e0001 ...` identifies employee `E0001`.
* HuntR searches the complete employee roster, so an employee hidden by the current `find` result can still be edited.
* After a successful edit, HuntR resets the list to show all employees, clearing any active `find` result.
* If an employee ID such as `E9999` does not exist, HuntR displays `No employee with ID E9999 was found.` and does not change any records.

Examples:
* `edit id/E0001 p/91234567 e/alex@example.com` updates the phone number and email address of employee `E0001`.
* `edit id/e0002 r/HR Manager` updates the role of employee `E0002`, using case-insensitive ID matching.

### Finding employees by name: `find`

Finds employees whose names contain any of the given keywords and displays them in the employee list.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Employees matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.
* At least one non-blank keyword must be provided.
* Each keyword must start with a letter or digit. Apostrophes, full stops, commas, slashes and hyphens are allowed
  after the first character.
* An argument beginning with `/` is rejected as an invalid command format.
* A search with no matching name is still valid.
* Leading and trailing spaces are ignored, and multiple spaces between keywords are treated as separators.
* A search with no matches is successful and displays an empty employee list.

Examples:
* `find John` returns employees named `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Archiving employees: `archive`

Marks an employee as archived. Archived employees remain stored but are hidden from normal `list` and `find` results.
After a successful archive, HuntR displays the full active employee list.

Format: `archive id/EMPLOYEE_ID`

Example: `archive id/E0123`

The ID is matched case-insensitively. An unknown ID produces `No employee with ID E0123 was found.` Archiving an
already archived employee produces `Employee with ID E0123 is already archived.` No data changes on either error.

### Unarchiving employees: `unarchive`

Restores an archived employee to the active roster.
After a successful unarchive, HuntR displays the full active employee list, including the restored employee.

Format: `unarchive id/EMPLOYEE_ID`

Example: `unarchive id/E0123`

An unknown ID produces the usual employee-not-found error. Attempting to unarchive an active employee produces
`Employee with ID E0123 is already active.`

### Listing archived employees: `list archived`

Shows only archived employees. Normal `list` continues to show only active employees.

Format: `list archived`

Archived employees retain their IDs, can be edited with `edit`, and can be deleted with `delete` (and restored with `undo` during the same session).

### Deleting an employee: `delete`

Deletes one employee from the complete roster using their employee ID.

Format: `delete id/EMPLOYEE_ID`

* Employee IDs contain 1 to 20 letters, digits, hyphens or underscores, with no spaces. For example, `E0001`, `EMP-0042`, `2024-017`, `abc` and `e0123` are valid.
* ID matching ignores letter case: `e0123` identifies the same employee as `E0123`. The success message shows the ID as stored.
* Supply exactly one `id/` parameter. Surrounding spaces and tabs are accepted; spaces within an ID, repeated `id/` parameters and other parameters are rejected.
* The command searches all employee records, including archived employees and employees hidden by `find`.
* The current search filter and the order of remaining employees are preserved.
* Deletion is immediate, with no confirmation. Use `undo` to restore it during the same session. The final employee can be deleted, and a deleted ID can be reused.

Examples, assuming the target employee exists:

* `delete id/EMP-0042` deletes employee `EMP-0042`.
* `delete id/e0123` deletes employee `E0123`.
* `find Betsy` followed by `delete id/EMP-0042` deletes `EMP-0042` even when hidden by that search. The Betsy search remains active.

On success, the message starts with `Deleted employee:` and includes the deleted employee's ID, name, phone, email, department and role.

The following input errors leave the records unchanged. Each message is followed by the usage block below.

| Input | Error message |
| --- | --- |
| `delete` or `delete 1` | `Missing parameter: id/EMPLOYEE_ID.` |
| `delete id/` or a whitespace-only ID | `Employee ID cannot be blank.` |
| `delete x/value` or `delete id/abc x/value` | `Unknown parameter: x/. Only id/ is supported.` |
| `delete id/abc!`, `delete id/abc def` or an ID longer than 20 characters | `Employee ID should be 1 to 20 characters long and contain only letters, digits, hyphens and underscores.` |
| `delete id/abc id/abc` | `Multiple values specified for the following single-valued field(s): id/` |
| `delete extra id/abc` | `Unexpected arguments. Specify exactly one employee ID using id/EMPLOYEE_ID.` |

```text
delete: Deletes one employee by employee ID from all stored employee records.
Parameters: id/EMPLOYEE_ID
Employee ID should be 1 to 20 characters long and contain only letters, digits, hyphens and underscores.
Example: delete id/EMP-0042
```

A valid ID that does not exist produces `No employee with ID EMP-0042 was found.` (using the entered ID), without the usage block. No records are changed. This also applies to deleting an employee twice or deleting from an empty roster.

### Clearing active employees: `clear`

Clears the active employee roster from HuntR. Archived employee records are preserved and can still be removed with
`delete`.

Format: `clear`

<box type="warning" seamless>

**Caution:** There is no confirmation. After `clear`, the empty active roster and unchanged archive are
saved immediately. Use `undo` to restore the active roster during the same session. Clearing an already empty
active roster still succeeds and shows the active view, but creates no history entry.
</box>

### Undoing and redoing employee changes: `undo`, `redo`

Format: `undo` or `redo`. Use lowercase command words without parameters. Surrounding spaces and tabs are accepted.
Each command moves exactly one history step.

| Input and condition | Exact result |
| --- | --- |
| `undo`, with a preceding state | `Undo successful.` |
| `redo`, with a subsequent state | `Redo successful.` |
| `  undo  `, with a preceding state | `Undo successful.` |
| `undo`, with no preceding state | `No more commands to undo.` |
| `redo`, with no subsequent state | `No more commands to redo.` |

Arguments such as `undo 2` or `undo id/E0123` produce:

```text
Invalid command format.
undo: Reverts the most recent data-changing command.
Parameters: none
Example: undo
```

Arguments such as `redo 2` or `redo now` produce:

```text
Invalid command format.
redo: Reapplies the most recently undone data-changing command.
Parameters: none
Example: redo
```

Syntax is checked before history availability, so these format errors also apply to a new session.
Aliases, step counts and uppercase command words are not supported. For example:

```text
Unknown command: UNDO
Type help to see the available commands.
```

The same template applies to `Undo`, `REDO`, `u`, `r` and `undo2`, preserving the entered command word.
Normal parsing and command errors retain your input and use the command box's existing error styling;
they do not save or change records, history or the view. Successful commands clear the command box.

One history step covers an entire successful `add`, `edit`, `delete`, `clear`, `import`, `archive` or `unarchive`.
An import of several employees is reversed by one undo. Redo restores saved data directly, even if the original
CSV has changed or been deleted. All employee values, ID spelling, archive status and stored order are restored,
including employees hidden from the current view. Clear affects only active employees.

Undo and redo preserve the view active immediately before the command: active-only, archived-only or the current
search, even when it matches nobody. Restored employees can remain hidden; use `list` or `list archived` to see them.
A historical search is not restored. Selection and scroll position are not guaranteed to remain unchanged.

A successful actual employee-data change after undo discards the redo states. An identical edit or an empty
active-roster clear creates no history entry, preserves redo, and retains its existing success message and reset
to the active view. Failed commands and `list`, `list archived`, `find`, `help`, `export`, `backup` and `exit` preserve
redo. Exported and backup files are outside employee history. Header-only imports remain errors.

History has no explicit capacity limit and exists only for this session. Starting HuntR loads the saved employee
state as one initial snapshot; neither undo nor redo is available until you make changes in that session.

### Exiting the program: `exit`

Closes HuntR after saving the current employee state. Session undo/redo history is discarded when HuntR closes.

Format: `exit`

The command takes no parameters, and extra text such as `exit now` is rejected.

### Saving the data

HuntR automatically saves the current employee state after every successfully executed command, including
read-only commands and commands that change nothing. You do not need to save manually. History and view filters
are never written to JSON.

If a data-changing command (including undo or redo) fails during execution or automatic saving, HuntR restores
its previous in-memory employee data, complete history and current view. A failed save is not reported as a
successful command. The automatic-save messages retain the underlying exception's message:

```text
Could not save data due to the following error: <exception-message>
```

```text
Could not save data to file <exception-message> due to insufficient permissions to write to the file or the folder.
```

The permission message can contain more than the destination path. These normal errors retain the command text
and existing error styling. Unexpected runtime failures also restore in-memory state, but the existing UI does
not provide ordinary command-box handling for those exceptions.

Read-only commands retain their existing behavior on automatic-save failure: for example, a changed view stays
changed. Export/backup files already created remain on disk. This version restores memory on these failures;
it does not guarantee that a failed write leaves the disk file unchanged.

### Editing the data file

HuntR data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users may edit that file while HuntR is closed. There is no live reload or concurrent-writer handling.

Each employee in the `employees` array has an `archived` status. Employee IDs remain unique regardless of status.
Older valid files without the `archived` field continue to load, with those employees treated as active.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, HuntR rejects the whole file (it does not load only the valid employees) and starts with an empty employee list at the next run. The invalid file remains on disk until you run a command (HuntR saves after every command, which then overwrites it). Therefore, always back up the file before editing it.<br>
Furthermore, certain edits can cause HuntR to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer, run it, enter any command (such as `list`) so that the data file is created, and exit. Then overwrite that data file (`data/addressbook.json`) with the data file from your previous HuntR home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE`<br>e.g., `add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`
**Archive** | `archive id/EMPLOYEE_ID`<br>e.g., `archive id/E0123`
**Clear**  | `clear`
**Delete** | `delete id/EMPLOYEE_ID`<br> e.g., `delete id/EMP-0042`
**Edit**   | `edit id/EMPLOYEE_ID [n/NAME] [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [r/ROLE]`<br>e.g., `edit id/E0001 p/91234567 e/alex@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list` or `list archived`
**Undo**   | `undo`
**Redo**   | `redo`
**Unarchive** | `unarchive id/EMPLOYEE_ID`
**Help**   | `help`
**Exit**   | `exit`
