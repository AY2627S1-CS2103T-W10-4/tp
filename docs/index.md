---
  layout: default.md
  title: ""
---

# AddressBook Level-3

[![CI Status](https://github.com/se-edu/addressbook-level3/workflows/Java%20CI/badge.svg)](https://github.com/se-edu/addressbook-level3/actions)
[![codecov](https://codecov.io/gh/se-edu/addressbook-level3/branch/master/graph/badge.svg)](https://codecov.io/gh/se-edu/addressbook-level3)

![Ui](images/Ui.png)

## User interface

The UI mockup presents HuntR as a workforce management dashboard. The navigation bar on the left gives users quick access to the Overview, Employees, Teams, and Relationships pages. On the Overview page, summary cards show the total numbers of employees, departments, and follow-ups. Users can search for employees and scan key information such as employee ID, department, role, and reporting manager in the central table.

Selecting an employee displays their profile and reporting relationships in the panel on the right, including their manager, teammates, and direct reports. Users can also add, list, find, or edit employees through the command bar at the bottom; for example, they can type `find Maya Chen` and press <kbd>Enter</kbd>. This combination of visual navigation and keyboard commands helps users understand their workforce at a glance while completing common tasks efficiently.

**AddressBook is a desktop application for managing your contact details.** While it has a GUI, most of the user interactions happen using a CLI (Command Line Interface).

* If you are interested in using AddressBook, head over to the [_Quick Start_ section of the **User Guide**](UserGuide.html#quick-start).
* If you are interested in developing AddressBook, the [**Developer Guide**](DeveloperGuide.html) is a good place to start.


**Acknowledgements**

* Libraries used: [JavaFX](https://openjfx.io/), [Jackson](https://github.com/FasterXML/jackson), [JUnit5](https://github.com/junit-team/junit5)
