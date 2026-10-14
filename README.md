[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

# HuntR

![Ui](docs/images/Ui.png)

## About HuntR

HuntR is a desktop app for the sole HR administrator at a small or medium-sized company. It helps them keep employee records accurate and quickly find the information they need. It is optimised for administrators who prefer typing commands, while still showing results in a graphical interface.

This version (v1.3) provides the core employee-record features listed below. Views of reporting relationships and workforce summaries are planned for later versions.

## User interface

The main window has a menu bar (**File** and **Help**), a command box at the top, a result display below it, and the list of employees underneath. Each employee card shows the employee's ID, name, phone number, email, department and role. The status bar at the bottom shows where the data file is saved.

Type a command in the command box and press <kbd>Enter</kbd>. For example, `find John` shows only the employees whose names contain `John`, and `list` shows everyone again.

For the detailed documentation of this project, see the **[HuntR Product Website](https://ay2627s1-cs2103t-w10-4.github.io/tp/)** and the **[User Guide](https://ay2627s1-cs2103t-w10-4.github.io/tp/UserGuide.html)**.

## Features

The features below are available in v1.3. See the [User Guide](https://ay2627s1-cs2103t-w10-4.github.io/tp/UserGuide.html) for the full command formats.

### Add employees

HuntR allows HR administrators to add employees to their workforce records using the `add` command. Each employee is assigned a unique employee ID and can have their name, phone number, email, department, and role recorded.

Example:
`add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`

### List employees

HuntR allows HR administrators to view all employees currently stored in the application using the `list` command. Each employee is displayed with their employee ID, name, phone number, email, department, and role.

Example:
`list`

### Edit employees

HuntR allows HR administrators to update an employee's details using the employee's unique employee ID. Only the fields you give are changed, and the employee ID itself cannot be changed.

Example:
`edit id/E0123 p/98765432 r/Senior Engineer`

### Delete employees

HuntR allows HR administrators to remove an employee from the workforce records using the employee's unique employee ID.

Example:
`delete id/E0123`

### Find employees

HuntR allows HR administrators to find employees whose names contain any of the given keywords using the `find` command. Matching is case-insensitive and matches full words only.

Example: `find John`

### Automatic data persistence

HuntR automatically saves your workforce records to disk after every change, and reloads them the next time the application is launched — no manual save or load required.

### Exit

HuntR allows HR administrators to close the application using the `exit` command, ensuring all workforce records are safely saved before the session ends.

Example: `exit`

## Acknowledgements

HuntR is based on the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org/).
