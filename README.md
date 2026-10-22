[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

# HuntR

![Ui](docs/images/Ui.png)

## About HuntR

HuntR is a desktop app for the sole HR administrator at a small or medium-sized company. It helps them keep employee records accurate and quickly find the information they need. It is optimised for administrators who prefer typing commands, while still showing results in a graphical interface.

This version (v1.4, an alpha release) builds on the core employee-record features with filtering and sorting, archiving, reporting relationships, workforce statistics and data checks, CSV import and export, and undo and redo.

## User interface

The main window has a menu bar (**File** and **Help**), a command box at the top, a result display below it, and the list of employees underneath. The result display has a green stripe when a command worked and a red stripe when it failed. Each employee card shows an avatar with the employee's initials in the colour of their department, the name with the employee ID, the role, the phone number and email, and chips for the department, the manager (`Reports to E0004`) and the `Archived` status. The status bar at the bottom shows where the data file is saved.

Type a command in the command box and press <kbd>Enter</kbd>. For example, `find John` shows only the employees whose names contain the word `John`, and `list` shows everyone again.

In the command box, <kbd>&uarr;</kbd> and <kbd>&darr;</kbd> bring back the commands you typed before, <kbd>Tab</kbd> completes the command word, and <kbd>Esc</kbd> clears the box.

For the detailed documentation of this project, see the **[HuntR Product Website](https://ay2627s1-cs2103t-w10-4.github.io/tp/)** and the **[User Guide](https://ay2627s1-cs2103t-w10-4.github.io/tp/UserGuide.html)**.

## Features

The features below are available in v1.4. See the [User Guide](https://ay2627s1-cs2103t-w10-4.github.io/tp/UserGuide.html) for the full command formats.

### Add employees

HuntR allows HR administrators to add employees to their workforce records using the `add` command. Each employee is assigned a unique employee ID and can have their name, phone number, email, department, and role recorded.

Example:
`add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`

### List employees

HuntR allows HR administrators to view all employees currently stored in the application using the `list` command. Each employee is displayed with their employee ID, name, phone number, email, department, and role.

Use `list archived` to view only the archived employees.

Example:
`list`

### Edit employees

HuntR allows HR administrators to update an employee's details using the employee's unique employee ID. Only the fields you give are changed, and the employee ID itself cannot be changed.

A manager can be set or removed with `m/`. See [Reporting relationships](#reporting-relationships-and-team-structure).

Example:
`edit id/E0123 p/98765432 r/Senior Engineer`

### Delete employees

HuntR allows HR administrators to remove an employee from the workforce records using the employee's unique employee ID.

Example:
`delete id/E0123`

### Find employees

HuntR allows HR administrators to find employees whose names contain any of the given keywords using the `find` command. Matching is case-insensitive and matches full words only.

Example: `find John`

### Filter employees

HuntR allows HR administrators to show only the employees who match all the given fields, using the `filter` command with `id/`, `n/`, `d/` and `r/`. Matching is case-insensitive and matches whole words. Repeat a prefix to match any of its values.

Example: `filter d/Sales d/Finance r/Manager` shows the managers in Sales or in Finance.

### Sort employees

HuntR allows HR administrators to sort the displayed employees by `id`, `name`, `department` or `role`, in ascending order or with `desc` in descending order, using the `sort` command. Letter case and accents are ignored. Sorting only changes what is shown, and `list` shows the saved order again.

Example: `sort department desc`

### Archive employees

HuntR allows HR administrators to take an employee off the active list without deleting them, using `archive`, and to bring them back with `unarchive`. Archived employees are left out of `list`, `find`, `filter` and the statistics, and `list archived` shows them.

Example: `archive id/E0123`

### Reporting relationships and team structure

HuntR allows HR administrators to record who reports to whom. Use `manager` and `unassign-manager` to set and remove a manager, `team` to see an employee with their manager and direct reports, `org` to list everyone under an employee, and `chain` to see the reporting chain above an employee. A reporting cycle is rejected.

Example: `manager id/E0002 m/E0001`

### Workforce statistics and data checks

HuntR allows HR administrators to see the number of active employees by department and by role with `stats`, and to find employees who share a phone number or an email address with `check`.

### Import, export and backup

HuntR allows HR administrators to save all employees to a CSV file with `export`, to add the employees of a CSV file with `import` (all or nothing, and `undo` reverses it), and to save a copy of the data with `backup`.

Example: `export exports/staff.csv`

### Undo and redo

Use `undo` to reverse one employee-data change and `redo` to reapply it, including an entire import.
The current active, archived or search view is preserved. Identical edits and empty active-roster clears create
no history entry. A successful actual change after undo discards redo; failed and read-only commands preserve it.
History lasts for the current session only.

### Automatic data persistence

HuntR saves the current employee state after every successfully executed command and reloads it at the next launch.
If a data-changing command or its save fails, employee data, history and the view are restored in memory.
Employee files are replaced atomically: handled save failures preserve the last stored file. History is not saved.
See the [User Guide](docs/UserGuide.md#saving-the-data) for save errors and supported local-file protections.

### Exit

HuntR allows HR administrators to close the application using the `exit` command, saving the current employee state before the session ends.

Example: `exit`

## Acknowledgements

HuntR is based on the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org/).
