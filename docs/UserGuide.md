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
  For example, `help 123` is interpreted as `help`. The `backup` and `exit` commands also take no parameters but reject extra text, so `backup now` and `exit now` show an error. Likewise, `list` accepts only `archived` after it, so `list 1` shows an error.

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

Format: `add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE [m/MANAGER_ID]`

All six fields are required. The employee ID must be 1 to 20 characters long and contain only letters, digits,
hyphens and underscores. Employee IDs are compared case-insensitively, so `E0123` and `e0123` identify the
same employee. For example, if `E0123` is already in use, HuntR displays
`An employee with Employee ID E0123 already exists.` and does not add the employee.

Parameters can be supplied in any order. Optional `m/MANAGER_ID` assigns an existing employee as the manager.
Manager IDs are matched case-insensitively and saved using the spelling of the existing employee's ID.
An empty `m/`, unknown manager or self-assignment is rejected without changing the roster.
For example, append `m/E0001` to an add command to assign employee `E0001` as the new employee's manager.

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

Format: `edit id/EMPLOYEE_ID [n/NAME] [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [r/ROLE] [m/MANAGER_ID]`

* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* Use `m/MANAGER_ID` to change the manager, or an empty `m/` to remove the manager. Omitting `m/` preserves it.
* For example, `edit id/E0002 m/E0001` assigns manager `E0001`, and `edit id/E0002 m/` removes that assignment.
* The manager must exist in the complete roster. Self-reporting and reporting cycles are rejected without changes.
* Editing an archived employee preserves their archived status.
* The `id/` parameter identifies the employee to edit. The employee ID itself cannot be changed.
* Employee ID matching is case-insensitive: `edit id/e0001 ...` identifies employee `E0001`.
* HuntR searches the complete employee roster, so an employee hidden by the current `find` result can still be edited.
* After a successful edit, HuntR resets the list to show all employees, clearing any active `find` result.
* If an employee ID such as `E9999` does not exist, HuntR displays `No employee with ID E9999 was found.` and does not change any records.

Examples:
* `edit id/E0001 p/91234567 e/alex@example.com` updates the phone number and email address of employee `E0001`.
* `edit id/e0002 r/HR Manager` updates the role of employee `E0002`, using case-insensitive ID matching.

### Assigning a manager: `manager`

Assigns or changes an employee's direct manager using the complete roster, including employees hidden by a search.
Each employee can have at most one direct manager. Assigning a manager preserves all other employee details,
including archived status. The employee card shows the manager's ID, or `Unassigned` when no manager is set.

Format: `manager id/EMPLOYEE_ID m/MANAGER_ID`

Example: `manager id/E0002 m/E0001` makes employee `E0002` report to `E0001`.

Both IDs must exist. Matching ignores case and saved manager IDs and success messages use the roster's ID spelling.
On success, HuntR displays `Manager of E0002 set to E0001.` for the example above.

The following errors leave all records unchanged. These messages do not include a usage block. Assume `E0001`,
`E0002` and `E0003` exist, and `E9999` does not exist. The cycle example additionally assumes that `E0002` reports
to `E0001`, and `E0003` reports to `E0002`.

| Input | Error message |
| --- | --- |
| `manager id/E0001 m/E0001` (self-reporting) | `An employee cannot report to themselves.` |
| `manager id/E0001 m/E9999` (unknown manager) | `Manager does not exist: E9999` |
| `manager id/E9999 m/E0001` (unknown employee) | `No employee with ID E9999 was found.` |
| `manager id/E0001 m/E0003` (reporting cycle) | `Reporting relationships cannot contain a cycle.` |

These manager validation messages also apply to `add` and `edit` when assigning a manager. See
[Reporting command input rules](#reporting-command-input-rules) for malformed arguments.

### Removing a manager: `unassign-manager`

Removes an employee's direct manager. The employee and their own reports remain in the roster.

Format: `unassign-manager id/EMPLOYEE_ID`

Example: `unassign-manager id/E0002`. Equivalently, use `edit id/E0002 m/`.

On success, HuntR displays `Manager of E0002 unassigned.`. An employee who already has no manager remains unchanged,
and the command still succeeds. `unassign-manager id/E9999` produces `No employee with ID E9999 was found.` if
`E9999` does not exist; no records change.

### Viewing an employee's team: `team`

Shows the employee, their direct manager (if assigned), and their direct reports. The result message gives the
manager's name and ID (or `None`) and the number of direct reports. Indirect reports are excluded.

Format: `team id/EMPLOYEE_ID`

Example: `team id/E0002`. After `manager id/E0002 m/E0001` and `manager id/E0003 m/E0002`,
this shows `E0001`, `E0002` and `E0003`, and reports one direct report for `E0002`.
An employee with no manager and no reports is shown on their own with a direct-report count of zero.

### Viewing all reports: `org`

Shows all direct and indirect reports under the specified employee. The employee themselves is excluded.
The result message lists descendants in breadth-first order; employee cards retain roster order.

Format: `org id/EMPLOYEE_ID`

Example: `org id/E0001`. With `E0002` reporting to `E0001` and `E0003` reporting to `E0002`,
the result includes `E0002` and `E0003`. With no reports, the list is empty and the message says `None.`

### Viewing the reporting chain: `chain`

Shows the specified employee's managers, from their immediate manager to the top of the hierarchy.
The employee themselves is excluded. The result message shows chain order using `->`; employee cards retain roster order.

Format: `chain id/EMPLOYEE_ID`

Example: `chain id/E0003`. With `E0003` reporting to `E0002` and `E0002` reporting to `E0001`,
the message lists `E0002 -> E0001` (with their names). With no manager, the list is empty and the message says `None.`

### Reporting command input rules

For `manager`, `unassign-manager`, `team`, `org` and `chain`, IDs must contain 1 to 20 letters, digits, hyphens or
underscores. Supply exactly one `id/` parameter. Only `manager` also accepts (and requires) one `m/` parameter.
Parameters can be in either order, with surrounding whitespace ignored. A blank manager is rejected by `manager`;
use `unassign-manager` or `edit id/EMPLOYEE_ID m/` to remove an assignment.

The following table gives the error text before the command-specific usage block. Each error leaves records and
the current list unchanged. These examples use `team`; the equivalent malformed arguments on the other reporting
commands append those commands' own usage instead.

| Input | Error message before usage |
| --- | --- |
| `team` (missing ID) | `Invalid command format.` |
| `team id/` (blank ID) | `Employee ID should not be blank.` |
| `team id/@@@` or `team id/E0001 extra` | `Employee ID should be 1 to 20 characters long and contain only letters, digits, hyphens and underscores.` |
| `team id/ABCDEFGHIJKLMNOPQRSTU` (21 characters) | `Employee ID should be at most 20 characters long.` |
| `team id/E0001 id/E0002` | `Multiple values specified for the following single-valued field(s): id/` |
| `team extra id/E0001` | `Invalid command format.` |
| `team id/E0001 x/foo` | `Invalid command format.` followed by `Unknown parameter: x/` |
| `manager id/E0001` (missing manager) | `Invalid command format.` |
| `manager id/E0001 m/` (blank manager) | `Employee ID should not be blank.` |

For example, entering `team` displays:

```text
Invalid command format.
team: Shows an employee, their manager and direct reports.
Parameters: id/EMPLOYEE_ID
Example: team id/E0002
```

A valid but unknown target ID produces `No employee with ID E9999 was found.` for `id/E9999`, without usage.
This applies to `team`, `org`, `chain` and `unassign-manager` as well as `manager`. It leaves records and the current
list unchanged.

Reporting queries replace the current search filter and leave records unchanged. They query relationships across
the complete stored roster, so counts and messages include archived employees. As with normal employee listings,
archived employees are hidden from the cards; use `list archived` to view them. Use `list` to return to all active employees.

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

Archived employees retain their IDs, can be edited with `edit`, and can be permanently deleted with `delete`.

### Deleting an employee: `delete`

Permanently deletes one employee from the complete roster using their employee ID.

Format: `delete id/EMPLOYEE_ID`

* Employee IDs contain 1 to 20 letters, digits, hyphens or underscores, with no spaces. For example, `E0001`, `EMP-0042`, `2024-017`, `abc` and `e0123` are valid.
* ID matching ignores letter case: `e0123` identifies the same employee as `E0123`. The success message shows the ID as stored.
* Supply exactly one `id/` parameter. Surrounding spaces and tabs are accepted; spaces within an ID, repeated `id/` parameters and other parameters are rejected.
* The command searches all employee records, including archived employees and employees hidden by `find`.
* The current search filter and the order of remaining employees are preserved.
* Deleting a manager removes the manager assignment from their direct reports, including archived reports. The success message gives the number affected. Other reporting relationships are preserved.
* Deletion is immediate, with no confirmation or undo. The final employee can be deleted, and a deleted ID can be reused.

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
`delete`. Manager assignments on archived employees are cleared if their manager is removed by `clear`.

Format: `clear`

<box type="warning" seamless>

**Caution:** There is no confirmation and no undo. After `clear`, the empty active roster and retained archived records are
saved immediately.
</box>

### Backing up the data: `backup`

Saves a copy of all current employee data to a new file in the `data/backups` folder.

Format: `backup`

* The backup is named by the date and time it was made, as `huntr-YYYYMMDD-HHmmss.json`, for example `data/backups/huntr-20261022-153005.json`. The `data/backups` folder is created if needed.
* The backup uses the same format as the data file `data/addressbook.json` and contains all employees, including employees hidden by `find`.
* Each backup is a new file, so earlier backups are kept. Two backups made within the same second share a name, and the later one replaces the earlier one.
* To restore a backup, exit HuntR, then replace `data/addressbook.json` with a copy of the backup file renamed to `addressbook.json`.
* The command takes no parameters, and extra text such as `backup now` is rejected.

On success, HuntR shows `Backed up N employee(s) to data/backups/huntr-YYYYMMDD-HHmmss.json`.

If the backup cannot be saved, for example because HuntR is not allowed to write to the `data` folder, HuntR shows `Could not save the backup to FILE_PATH. Check that you are allowed to write to that folder.`

### Exiting the program: `exit`

Closes HuntR. Your employee records are saved after every command, so nothing is lost when you exit.

Format: `exit`

The command takes no parameters, and extra text such as `exit now` is rejected.

### Saving the data

HuntR automatically saves data after every command. You do not need to save manually.

### Editing the data file

HuntR data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

Each employee in the `employees` array has an `archived` status. Employee IDs remain unique regardless of status.
Older valid files without the `archived` field continue to load, with those employees treated as active.
The optional `managerId` field stores the direct manager's ID. Missing or null `managerId` means no manager.
Manager IDs must refer to an existing employee, with no self-reporting or cycles. Invalid relationships cause the
whole file to be rejected.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, HuntR rejects the whole file (it does not load only the valid employees) and starts with an empty employee list at the next run. The invalid file remains on disk until you run a command (HuntR saves after every command, which then overwrites it). Therefore, always back up the file (for example with `backup`) before editing it.<br>
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
**Add**    | `add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE [m/MANAGER_ID]`<br>e.g., `add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`
**Archive** | `archive id/EMPLOYEE_ID`<br>e.g., `archive id/E0123`
**Backup** | `backup`
**Clear**  | `clear`
**Delete** | `delete id/EMPLOYEE_ID`<br> e.g., `delete id/EMP-0042`
**Edit**   | `edit id/EMPLOYEE_ID [n/NAME] [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [r/ROLE] [m/MANAGER_ID]`<br>e.g., `edit id/E0001 p/91234567 e/alex@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list` or `list archived`
**Manager** | `manager id/EMPLOYEE_ID m/MANAGER_ID`
**Unassign manager** | `unassign-manager id/EMPLOYEE_ID`
**Team** | `team id/EMPLOYEE_ID`
**Org** | `org id/EMPLOYEE_ID`
**Chain** | `chain id/EMPLOYEE_ID`
**Unarchive** | `unarchive id/EMPLOYEE_ID`
**Help**   | `help`
**Exit**   | `exit`
