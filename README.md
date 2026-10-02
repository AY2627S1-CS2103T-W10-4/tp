[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

# HuntR

![Ui](docs/images/Ui.png)

* This is **a sample project for Software Engineering (SE) students**.<br>
  Example usages:
  * as a starting point of a course project (as opposed to writing everything from scratch)
  * as a case study
* The project simulates an ongoing software project for a desktop application (called _AddressBook_) used for managing contact details.
  * It is **written in an object-oriented programming (OOP) style** and provides a **reasonably well-written** codebase of about 6 KLoC. It is **larger** than what students typically write in beginner-level software-engineering modules, without being overwhelming.
  * It comes with a **reasonable level of user and developer documentation**.
* It is named `AddressBook Level 3` (`AB3` for short) because it was initially created as a part of a series of `AddressBook` projects (`Level 1`, `Level 2`, `Level 3` ...).
* For the detailed documentation of this project, see the **[HuntR Product Website](https://ay2627-cs2103t-w10-4.github.io/tp/)**.

## Features

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

This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).

