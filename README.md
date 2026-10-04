[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

# HuntR

![Ui](docs/images/Ui.png)

HuntR is a desktop app for the sole HR administrator at a small or medium-sized company. It helps them keep employee records accurate, see how staff relate to each other, such as who reports to whom and who works in which team, and get an overall picture of the workforce. It is optimised for administrators who prefer typing commands, while still showing results in a graphical interface.

For the detailed documentation of this project, see the **[HuntR Product Website](https://ay2627-cs2103t-w10-4.github.io/tp/)**.

For detailed documentation, see the **[HuntR Product Website](https://ay2627s1-cs2103t-w10-4.github.io/tp/)**.

## Planned features

The features below describe the intended employee-management functionality. Employee-specific fields and commands are still under development.

### Add employees

HuntR allows HR administrators to add employees to their workforce records using the `add` command. Each employee is assigned a unique employee ID and can have their name, phone number, email, department, and role recorded.

Example:
`add id/E0123 n/John Tan p/91234567 e/johntan@example.com d/Engineering r/Software Engineer`

### List employees

HuntR allows HR administrators to view all employees currently stored in the application using the `list` command. Each employee is displayed with their employee ID, name, phone number, email, department, and role.

Example:
`list`

### Delete employees

HuntR allows HR administrators to remove an employee from the workforce records using the employee's unique employee ID.

Example:
`delete id/E0123`

## Acknowledgements

HuntR is based on the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org/).
