---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# HuntR User Guide

HuntR is a **desktop application for a sole HR administrator to manage employee records, optimised for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you can type quickly, HuntR can help you add, find, update and remove employee records faster than traditional GUI-based HR tools.

HuntR is based on [AddressBook-Level3](https://github.com/se-edu/addressbook-level3) from SE-EDU. This guide covers version 1.4.

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

   * `sort department` : Sorts the displayed employees by department, from A to Z.

   * `add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer` : Adds an employee named `John Tan` to HuntR.

   * `edit id/E0123 p/92345678 r/Senior Software Engineer` : Updates the phone number and role of employee `E0123`.

   * `find Alex` : Finds employees whose names contain the word `Alex`.

   * `filter d/Engineering r/Manager` : Shows only the employees in the Engineering department whose role contains `Manager`.

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
  The `stats` and `check` commands take no parameters and reject extra text too.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Typing commands faster

These keys work in the command box:

| Key | What it does |
| --- | --- |
| <kbd>↑</kbd> | Brings back the command you typed before. Press it again to go further back. |
| <kbd>↓</kbd> | Goes forward again. After the newest command, it shows what you had been typing. |
| <kbd>Tab</kbd> | Completes the command word. `fil` becomes `filter `. When several commands start with what you typed, it completes as far as they are the same, so `u` becomes `un` (`undo`, `unarchive` and `unassign-manager`). |
| <kbd>Esc</kbd> | Clears the command box. |

* The commands you typed are kept until you close HuntR. Up to 100 are kept, and a command that gave an error is kept too, so you can bring it back and correct it.
* A command that is the same as the one before it is kept once.
* <kbd>Tab</kbd> only completes the first word, so it does nothing once you have typed a space. <kbd>Shift</kbd>+<kbd>Tab</kbd> still moves between parts of the window.

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
phone number, email, department, and role. Employees are shown in the order in which they were added, so `list` also undoes any `sort`.

Format: `list`

The command takes no parameters. Leading and trailing spaces are ignored, but additional text is rejected.

If there are no employee records, HuntR displays an empty employee list and explains how to add an employee.

Use `list` to show every employee again after a `find` or `filter`.

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
The result message lists descendants in breadth-first order. Employee cards preserve the current sort order,
or roster order when no sort is active. For example, after `sort id desc`, the cards remain sorted by descending ID.

Format: `org id/EMPLOYEE_ID`

Example: `org id/E0001`. With `E0002` reporting to `E0001` and `E0003` reporting to `E0002`,
the result includes `E0002` and `E0003`. With no reports, the list is empty and the message says `None.`

### Viewing the reporting chain: `chain`

Shows the specified employee's managers, from their immediate manager to the top of the hierarchy.
The employee themselves is excluded. The result message shows chain order using `->`. Employee cards preserve
the current sort order, or roster order when no sort is active.

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
the complete stored roster. Related archived employees are included in both the messages and employee cards,
so report counts and named managers agree with the displayed results. The current sort order is preserved.
Use `list` to return to all active employees, or `list archived` to show only archived employees.

Reporting relationships are also preserved by CSV export and import. `export FILE_PATH` writes the header
`id,name,phone,email,department,role,archived,managerId`; a blank `managerId` means no manager. For example,
use `export exports/staff.csv` and then `import exports/staff.csv` into an empty roster. Import accepts the older
six-column format and seven-column format ending in `archived`, treating those rows as having no manager.
A manager may appear later in the file or already exist in HuntR. Invalid IDs, missing managers, self-reporting
and cycles reject the import without adding any employees. Existing employee IDs are rejected rather than overwritten.

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

### Viewing workforce statistics: `stats`

Shows the total number of active employees and the employee counts for each department and role in the result box.

Format: `stats`

* Counts cover all active employees, including employees hidden by a search. Archived employees are excluded,
  even when `list archived` is currently displayed.
* Each breakdown is sorted by count, largest first. Ties are sorted by the stored department or role name
  in case-sensitive alphabetical order (uppercase letters before lowercase letters).
* Department and role names are grouped exactly as stored: `Ops` and `ops` are separate groups.
* The employee records and current employee list view are unchanged.
* If there are no active employees, the total is `0` and both breakdowns display `(none)`.

Example: `stats`, with two active employees in Ops and one in Sales, displays:

```text
Total active employees: 3
By department:
Ops: 2
Sales: 1
By role:
Engineer: 2
Manager: 1
```

The command accepts leading and trailing spaces but no arguments. For example, `stats d/Ops` and `stats 1`
are rejected with `Invalid command format.` followed by `Usage: stats`. No records or list filters change.

### Checking data quality: `check`

Lists active employees who share a phone number or email with another active employee in the result box.
Shared contact details may indicate a data-entry error; HuntR flags them for review without editing or deleting them.

Format: `check`

* Checks cover all active employees regardless of the current search or archive view. Archived employees neither
  appear in the report nor cause an active employee to be flagged.
* Contact values are compared exactly as stored, consistent with HuntR's phone and email equality rules.
  For example, `91234567` and `9123 4567` are different, as are `Alice@example.com` and `alice@example.com`.
* Every affected employee is counted and listed once, even if both their phone and email are shared or more
  than two employees share a value. Each entry includes their ID, name and shared contact details.
* Entries are sorted by employee ID, ignoring letter case. The current employee list view and records are unchanged.
* An empty active roster or a roster without shared contacts produces `No data-quality issues found.`

Example: if active employees E0001 (Alice) and E0002 (Bob) both have phone number `91234567`, `check` displays:

```text
Found 2 active employee(s) with data-quality issues:
E0001 (Alice): shared phone 91234567
E0002 (Bob): shared phone 91234567
```

Use `edit id/EMPLOYEE_ID ...` to correct a record after reviewing it, then run `check` again.

The command accepts leading and trailing spaces but no arguments. For example, `check id/E0001` and `check all`
are rejected with `Invalid command format.` followed by `Usage: check`. No records or list filters change.

### Filtering employees: `filter`

Shows only the employees who match the employee ID, name, department and role that you give.

Format: `filter [id/EMPLOYEE_ID] [n/NAME] [d/DEPARTMENT] [r/ROLE]`

* At least one of `id/`, `n/`, `d/` or `r/` must be given. If you give several, an employee must match **all** of them (an `AND` search), so the list is narrowed further.
* Matching is case-insensitive and uses whole words, like `find`. For example, `d/engineering` matches `Engineering`, and `d/Resources` matches `Human Resources`, but `d/Engineer` does not match `Engineering`.
* Spaces and punctuation separate words. For example, `d/R&D` matches the department `R&D, Asia`, and `r/Backend` or `r/(Backend)` matches the role `Software Engineer (Backend)`. The symbols `&` and `'` stay part of a word.
* If a value has several words, every word must appear in that field, in any order. For example, `r/Software Engineer` matches `Software Engineer` but not `Software Manager`.
* An employee ID must be the whole ID, such as `id/E0123`. The ID is not case-sensitive.
* To match any of several values of the same field (an `OR` search), repeat the prefix. For example, `d/Sales d/Finance` matches employees in `Sales` or in `Finance`. Each repeated value is a separate search, so `r/Manager r/Analyst` is not the same as `r/Manager Analyst`, which needs both words.
* Filtering only changes what is shown. Your saved data is not changed. Archived employees are not shown. Use [`list`](#listing-all-employees-list) to show all employees again.
* A filter with no matches is valid and shows an empty list with the message `0 employees listed!`.
* Leading and trailing spaces are ignored, and the parameters can be in any order.

Examples:
* `filter d/Engineering` shows everyone in the Engineering department.
* `filter r/Manager` shows employees whose role contains the word `Manager`, such as `Engineering Manager`.
* `filter d/Engineering r/Manager` shows only the Engineering employees whose role contains `Manager`.
* `filter n/John d/Sales` shows only the employees named `John` who are in Sales. Other people in Sales, and Johns in other departments, are not shown.
* `filter d/Sales d/Finance r/Manager` shows the managers who are in Sales or in Finance.
* `filter id/E0123` shows only the employee with the ID `E0123`.

The following input errors leave the displayed list unchanged. Each message is followed by the usage of the command.

| Input | Error message |
| --- | --- |
| `filter` or `filter Engineering` | `Invalid command format!` |
| `filter e/john@example.com` or `filter x/foo` | `Unknown parameter: e/` (or the parameter given) |
| `filter d/` or `filter n/` | `The value of d/ should contain at least one letter or digit.` (or `n/`, `r/`) |
| `filter d/@@@` | `The value of d/ should contain at least one letter or digit.` |
| `filter id/E 1` | The employee ID constraints message |

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
* Deleting a manager removes the manager assignment from their direct reports, including archived reports. The success message gives the number affected. Other reporting relationships are preserved.
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

### Sorting employees: `sort`

Sorts the displayed employees by one field, in ascending or descending order.

Format: `sort FIELD [asc|desc]`

* `FIELD` must be one of `id`, `name`, `department` or `role`. It is not case-sensitive, so `Department` also works.
* The order is `asc` (A to Z) or `desc` (Z to A). It is not case-sensitive. If you leave it out, the order is ascending.
* Letter case and accents are ignored, so `Émile` is sorted with the names that start with `E`. A number inside a value is compared by its value, so `E2` comes before `E10`, and `Level 2` comes before `Level 10`; leading zeros do not change this, so `E0002` comes before `E10`. Otherwise text is compared character by character.
* Employees with the same value are always ordered by employee ID from A to Z, also when the order is `desc`.
* Sorting only changes the order in which employees are shown. Your saved data is not changed.
* The sorted order is kept when you use `find`, and when you add, edit or delete employees. An employee whose name, department or role is edited moves to the correct position.
* Use [`list`](#listing-all-employees-list) to show the employees in the order in which they were added again.

Examples:
* `sort name` shows the employees in alphabetical order of their names.
* `sort department desc` groups the employees by department, from `Sales` back to `Engineering`.
* `sort role asc` sorts the employees by their role, from A to Z.

The following input errors leave the displayed order unchanged. Each message is followed by the usage of the command.

| Input | Error message |
| --- | --- |
| `sort` or `sort name asc desc` | `Invalid command format!` |
| `sort salary` or `sort salary desc` | `Unknown sort field: salary. The field must be one of: id, name, department, role.` |
| `sort name down` | `Unknown sort order: down. The order must be one of: asc, desc.` |

### Clearing active employees: `clear`

Clears the active employee roster from HuntR. Archived employee records are preserved and can still be removed with
`delete`. Manager assignments on archived employees are cleared if their manager is removed by `clear`.

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

One history step covers an entire successful `add`, `edit`, `delete`, `clear`, `import`, `archive`, `unarchive`,
`manager` or `unassign-manager`.
An import of several employees is reversed by one undo. Redo restores saved data directly, even if the original
CSV has changed or been deleted. All employee values, ID spelling, archive status, manager assignments and stored order are restored,
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

HuntR automatically saves employee data after every successfully executed command, including commands such as
`list` and `help`, and commands that change nothing. You do not need to save manually. History and view filters
are never written to JSON.

HuntR writes the complete JSON to a protected temporary file beside the data file, then replaces the data file
atomically. If a handled failure occurs before replacement, the last stored file keeps its original bytes. If no
data file existed, it remains absent. Newly created folders may remain. Successful replacement completes the save.

If a data-changing command (including undo or redo) fails during execution or automatic saving, HuntR restores
its previous in-memory employee data, complete history and current view. A failed change after undo preserves
the original redo sequence. A failed no-op also restores the previous view and preserves redo.

Use HuntR as a single-user application on a local filesystem that supports atomic replacement and the required
access protections. New files are private to their creator: owner read/write (`0600`) on POSIX systems, or an
owner-only access ACL granting full file access on Windows. Existing files retain their nine POSIX permission bits
or supported Windows access ACL, provided their owner (and POSIX group) matches the newly created file's owner/group.
Read-only files are rejected; HuntR does not clear protection or change ownership to allow a save. Other filesystem
metadata is not promised to be preserved. See the [Developer Guide](DeveloperGuide.md#storage-component) for the
precise supported policy.

Configured paths stay unchanged. Valid symbolic links are retained and their real targets are saved; dangling
links and directory destinations are rejected. Do not run concurrent writers or edit the file while HuntR is open.

An automatic-save failure is displayed instead of the command's success result:

```text
Could not save data due to the following error: <exception-message>
```

For unsupported atomic replacement, the exact message is:

```text
Could not save data due to the following error: Atomic replacement is not supported for this data file.
```

If the required access protections cannot be established or preserved, it is:

```text
Could not save data due to the following error: Cannot preserve the required file access protections.
```

Access-denied failures use:

```text
Could not save data to file <exception-message> due to insufficient permissions to write to the file or the folder.
```

Here, `<exception-message>` is the filesystem exception's message. It may include paths and additional diagnostic
text, rather than just the configured destination path. These normal errors retain the command text and existing
error styling. Unexpected runtime failures also restore in-memory state for data-changing commands, but the
existing UI does not provide ordinary command-box handling for those exceptions.

Read-only commands retain their existing behavior on automatic-save failure: for example, a changed view stays
changed. Export/backup files already created remain on disk.

JSON backups use the same safer writer and keep their existing location and naming. A backup write failure,
including unsupported atomic movement or protection, keeps its backup-specific message:

```text
Could not save the backup to <backup-path>. Check that you are allowed to write to that folder.
```

Successful backups still report `Backed up <count> employee(s) to <backup-path>`. If the backup is created but the
following automatic save fails, the backup remains and the final result is the automatic-save error above.
CSV exports and preference saving are unchanged.

### Editing the data file

HuntR data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users may
update that file in a text editor while HuntR is closed.

Each employee in the `employees` array has an `archived` status. Employee IDs remain unique regardless of status.
Older valid files without the `archived` field continue to load, with those employees treated as active.
The optional `managerId` field stores the direct manager's ID. Missing or null `managerId` means no manager.
Manager IDs must refer to an existing employee, with no self-reporting or cycles. Invalid relationships cause the
whole file to be rejected.

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
**Add**    | `add id/EMPLOYEE_ID n/NAME p/PHONE e/EMAIL d/DEPARTMENT r/ROLE [m/MANAGER_ID]`<br>e.g., `add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`
**Archive** | `archive id/EMPLOYEE_ID`<br>e.g., `archive id/E0123`
**Backup** | `backup`
**Check**  | `check`
**Clear**  | `clear`
**Delete** | `delete id/EMPLOYEE_ID`<br> e.g., `delete id/EMP-0042`
**Edit**   | `edit id/EMPLOYEE_ID [n/NAME] [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [r/ROLE] [m/MANAGER_ID]`<br>e.g., `edit id/E0001 p/91234567 e/alex@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**Filter** | `filter [id/EMPLOYEE_ID] [n/NAME] [d/DEPARTMENT] [r/ROLE]`<br> e.g., `filter n/John d/Sales`, `filter d/Sales d/Finance r/Manager`
**List**   | `list` or `list archived`
**Stats**  | `stats`

**Manager** | `manager id/EMPLOYEE_ID m/MANAGER_ID`
**Unassign manager** | `unassign-manager id/EMPLOYEE_ID`
**Team** | `team id/EMPLOYEE_ID`
**Org** | `org id/EMPLOYEE_ID`
**Chain** | `chain id/EMPLOYEE_ID`
**Undo**   | `undo`
**Redo**   | `redo`
**Unarchive** | `unarchive id/EMPLOYEE_ID`
**Help**   | `help`
**Sort**   | `sort FIELD [asc\|desc]`<br> e.g., `sort department desc`
**Exit**   | `exit`
