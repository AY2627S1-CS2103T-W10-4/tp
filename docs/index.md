---
  layout: default.md
  title: ""
---

# HuntR

[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

![Ui](images/Ui.png)

## About HuntR

**HuntR is a desktop app for the sole HR administrator at a small or medium-sized company.** It helps them keep employee records accurate and quickly find the information they need. It is optimised for administrators who prefer typing commands, while still showing results in a graphical interface.

This version (v1.5) lets you add, list, find, filter, sort, edit and delete employees, archive and unarchive them, record who reports to whom, see workforce statistics and data checks, import and export CSV files, back up the data and restore a backup, and undo and redo changes, with automatic saving.

## User interface

The main window has a menu bar (**File** and **Help**), a command box at the top, and the list of employees underneath. After a command, a result strip appears below the command box with a green stripe when the command worked and a red stripe when it failed. Each employee card shows its position in the list (`01`, `02` and so on), an avatar with the employee's initials in the colour of their department, the name with the employee ID, the role, the phone number and email, a chip for the department (and an `Archived` chip for archived employees), and the manager (`Reports to E0004`, or `Unassigned`). The status bar at the bottom shows where the data file is saved.

Type a command in the command box and press <kbd>Enter</kbd>. For example, `find John` shows only the employees whose names contain the word `John`, and `list` shows everyone again.

In the command box, <kbd>&uarr;</kbd> and <kbd>&darr;</kbd> bring back the commands you typed before, <kbd>Tab</kbd> completes the command word, and <kbd>Esc</kbd> clears the box.

* If you are interested in using HuntR, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing HuntR, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.

**Acknowledgements**

* HuntR is based on the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org/).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
