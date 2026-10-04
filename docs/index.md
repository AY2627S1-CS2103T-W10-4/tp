---
  layout: default.md
  title: ""
---

# HuntR

[![Java CI](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/AY2627S1-CS2103T-W10-4/tp/actions/workflows/gradle.yml)

![Ui](images/Ui.png)

**HuntR is a desktop app for the sole HR administrator at a small or medium-sized company.** It helps them keep employee records accurate, see how staff relate to each other, such as who reports to whom and who works in which team, and get an overall picture of the workforce. It is optimised for administrators who prefer typing commands, while still showing results in a graphical interface.

Employee-specific fields and commands are still under development. The guides describe the current application and its planned development.

* If you are interested in using HuntR, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing HuntR, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.

**Acknowledgements**

* HuntR is based on the [AddressBook-Level3 (AB3)](https://github.com/se-edu/addressbook-level3) project created by the [SE-EDU initiative](https://se-education.org/).
* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)

## User interface

The UI mockup presents HuntR as a workforce management dashboard. The navigation bar on the left gives users quick access to the Overview, Employees, Teams, and Relationships pages. On the Overview page, summary cards show the total numbers of employees, departments, and follow-ups. Users can search for employees and scan key information such as employee ID, department, role, and reporting manager in the central table.

Selecting an employee displays their profile and reporting relationships in the panel on the right, including their manager, teammates, and direct reports. Users can also add, list, find, or edit employees through the command bar at the bottom; for example, they can type `find Maya Chen` and press <kbd>Enter</kbd>. This combination of visual navigation and keyboard commands helps users understand their workforce at a glance while completing common tasks efficiently.