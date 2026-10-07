---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete id/E0003` : Permanently deletes employee `E0003`, including if hidden by a search.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

The help window displays `Refer to the user guide: https://ay2627-cs2103t-w10-4.github.io/tp/UserGuide.html`. Its copy button copies the HuntR User Guide link.

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... `

<box type="tip" seamless>

**Tip:** A person can have any number of tags, including zero.
</box>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Only full words match; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting an employee: `delete`

Permanently removes one employee from all stored workforce records.

Format: `delete id/EMPLOYEE_ID`

* The ID must be uppercase `E` followed by exactly four ASCII digits: `E0000` through `E9999`.
* Both `delete` and `id/` are lowercase. Lowercase IDs such as `e0123` are rejected, not converted.
* Spaces and tabs are allowed around the command, between tokens, and after `id/`. Whitespace inside the ID is invalid.
* Exactly one ID is required. Repeated `id/` prefixes (even identical ones), extra fields, and extra text are rejected.
* The command searches the **whole roster**, including employees hidden by `find`. Names and displayed row numbers do not identify the target.
* The current search filter and the order of remaining employees are preserved. An empty view stays an empty list.
* There is **no confirmation or undo**. Deleting the last employee is allowed and stays empty after restarting.
* Success is shown only after saving. A save failure leaves both the running roster and previous data file unchanged.
* A deleted employee ID can be reused by `add`.

Examples (assuming the target exists):

* `delete id/E0123` deletes employee `E0123`.
* `delete    id/ E0123` performs the same deletion.
* `find Betsy` followed by `delete id/E0123` deletes `E0123` even when that employee is not among the results. The Betsy search remains active.

For John Tan (`E0123`, phone `91234567`, email `johntan@example.com`, department `Engineering`, role `Software Engineer`), success is exactly:

```text
Deleted employee: ID: E0123; Name: John Tan; Phone: 91234567; Email: johntan@example.com; Department: Engineering; Role: Software Engineer
```

Every syntax or ID-validation error below appends this usage block after a newline:

```text
delete: Deletes one employee by employee ID from all stored employee records.
Parameters: id/EMPLOYEE_ID (uppercase E followed by exactly four digits)
Example: delete id/E0123
```

| Invalid input | Message before the usage block |
| --- | --- |
| `delete`, `delete id/`, `delete 1`, `delete E0123`, `delete ID/E0123`, `delete extra id/E0123` | `Invalid command format!` |
| `delete id/e0123`, `delete id/EMP-0042`, `delete id/123`, `delete id/E123`, `delete id/E01234`, `delete id/E 0123`, `delete id/E0123,E0456` | `Employee ID must be an uppercase E followed by exactly four digits (e.g. E0123).` |
| `delete id/E0123 extra`, `delete id/E0123 E0456`, `delete id/E0123 n/John` | `Unexpected arguments. Specify exactly one employee ID.` |
| `delete id/E0123 id/E0123`, `delete id/E0123 id/E0456` | `Multiple values specified for the following single-valued field(s): id/` |

Other outcomes do not append usage:

* `DELETE id/E0123`: `Unknown command.`
* An absent ID, including repeated deletion or an empty roster: `No employee with ID E0123 was found.`
* A permission failure: `Could not delete employee E0123: insufficient permission to save employee records. No employee records were changed.`
* Another save failure: `Could not delete employee E0123: employee records could not be saved. No employee records were changed.`

On failure, the roster and filter remain unchanged, and the command stays in the input box with error styling so it can be corrected or retried. On success, the input box clears. The existing result log includes the deleted employee's displayed details.

**Compatibility:** `delete INDEX` has been removed. `edit` remains index-based and cannot change an employee's ID. The strict `E####` constraint also applies to `add`, model validation, and data-file loading. Existing files with valid IDs need no migration; a file containing any lowercase or custom-format ID is rejected as a whole. There is no automatic ID conversion. As with other invalid files, startup uses an empty roster and logs a warning without changing the file; later successful record-changing commands can overwrite it with the running roster.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

HuntR automatically saves employee records for `add`, `edit`, `delete`, and `clear`. You do not need to save manually. Each command updates the running roster and reports success only after saving succeeds. If saving fails, the running records, active search, and previously saved file remain unchanged, so you can fix the storage problem and retry the same command.

Saving prepares a replacement file and swaps it into place in one step. If your storage location does not support that operation, the command reports a save error and leaves your records unchanged. Use a storage location that supports atomic file replacement.

`list`, `find`, `help`, and `exit` do not write employee records and remain available when saving is unavailable. Successful `add` and `edit` commands show all employees as before; `delete` and `clear` retain the active filter. Deleting the final employee or clearing the roster saves an empty `employees` array, which remains empty on restart. `clear` saves even when the running roster is already empty.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, HuntR starts with an empty roster at the next run. The invalid file remains unchanged until a record-changing command saves successfully. `list`, `find`, `help`, and `exit` do not overwrite it. Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... ` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Clear**  | `clear`
**Delete** | `delete id/EMPLOYEE_ID`<br> e.g., `delete id/E0123`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List**   | `list`
**Help**   | `help`
