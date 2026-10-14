---
  layout: default.md
  title: ""
---

# HuntR

[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

![Ui](images/Ui.png)

## About HuntR

**HuntR is a desktop app for the sole HR administrator at a small or medium-sized company.** It helps them keep employee records accurate and quickly find the information they need. It is optimised for administrators who prefer typing commands, while still showing results in a graphical interface.

This version (v1.3) provides the core employee-record features: add, list, find, edit and delete employees, with automatic saving. Views of reporting relationships and workforce summaries are planned for later versions.

## User interface

The main window has a menu bar (**File** and **Help**), a command box at the top, a result display below it, and the list of employees underneath. Each employee card shows the employee's ID, name, phone number, email, department and role. The status bar at the bottom shows where the data file is saved.

Type a command in the command box and press <kbd>Enter</kbd>. For example, `find John` shows only the employees whose names contain `John`, and `list` shows everyone again.

* If you are interested in using HuntR, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing HuntR, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.

**Acknowledgements**

* HuntR is based on the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org/).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
