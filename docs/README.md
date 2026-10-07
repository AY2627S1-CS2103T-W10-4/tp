---
  layout: default.md
  title: "Documentation overview"
  pageNav: 3
---

# HuntR documentation

* [User Guide](UserGuide.md): command syntax, exact output, errors, and compatibility.
* [Developer Guide](DeveloperGuide.md): deletion design, persistence, diagrams, and manual checks.
* [Testing guide](Testing.md): test tooling. The repository's `tests/test-plan.md` contains the complete delete-employee acceptance plan.
* [Documentation guide](Documentation.md): maintaining the MarkBind website.

## Delete employee

```text
delete id/EMPLOYEE_ID
```

Deletion permanently removes one employee from the complete roster, including employees hidden by a search. IDs must match `E[0-9]{4}`: uppercase `E` plus exactly four ASCII digits. Spaces and tabs around tokens are accepted. Extra fields, repeated IDs, lowercase IDs, custom ID formats, and the old `delete INDEX` syntax are rejected.

The active search and remaining order are preserved. Success includes every stored employee field and is reported only after saving. On a save failure, both the running roster and previously saved file remain unchanged. The last employee may be deleted, and a deleted ID may be reused.

Deletion is staged in a detached model. Storage writes a sibling temporary file and atomically replaces the saved roster; it fails safely when atomic replacement is unsupported. The live model is updated only after saving. There are no confirmation prompts, undo, deletion archives, reserved IDs, or new JSON fields.

## Compatibility and scope

* The JSON schema is unchanged: an `employees` array whose records contain `id`, `name`, `phone`, `email`, `department`, and `role`.
* Strict IDs apply to additions, model construction, and file loading. Existing valid IDs need no migration. Any invalid ID rejects the entire file under existing startup behavior; there is no automatic conversion.
* A rejected file is unchanged at startup. Later successful record-changing commands can overwrite it with the running roster; `list`, `find`, `help`, and `exit` leave it untouched.
* The existing index-based `edit` command retains IDs and does not support changing them. This corrects the specification's assumption about ID editing without adding an adjacent feature.
* Normal command layout, cards, input behavior, and preferences storage are retained. Saving is standardized across record-changing commands as described below.

## Shared saving behavior

`add`, `edit`, `delete`, and `clear` execute on a detached copy of the roster and active filter. They share the atomic save method and update the live records only after saving succeeds. A failed save leaves the live roster, filter, order, and saved file unchanged, and the same command can be retried. Unsupported atomic replacement is a safe failure for every record-changing command.

The copied filter ensures `edit 1` still edits the first displayed employee. After success, add and edit show all employees; delete and clear preserve the filter. Existing command messages are unchanged. `clear` on an empty roster and an edit that repeats an existing value still save.

`list`, `find`, `help`, and `exit` do not save employee records, so they work even if saving is unavailable. This follow-up intentionally replaces the earlier save-after-every-command behavior. `LogicManagerIntegrationTest` and the storage failure tests cover the shared behavior.

## Verify documentation

Java and Graphviz (`dot`) are needed to render every diagram, as in the documentation CI workflow. Inspect generated diagrams as well as the build exit status: MarkBind can report success even if a diagram renderer fails.

From the repository root:

```sh
npm ci --prefix docs --ignore-scripts
npm run build --prefix docs
```

The build generates `docs/_site/`. Do not commit generated site files or installed dependencies. Check the updated deletion section and diagrams in the rendered site.

Help displays and copies the HuntR User Guide destination currently declared by the project:
[HuntR User Guide](https://ay2627-cs2103t-w10-4.github.io/tp/UserGuide.html).
Verify the published URL before release and keep it consistent with the root README and `HelpWindow`. A local documentation build does not deploy the site.

Run `./gradlew check coverage` with Java 25 for the automated feature gate. Cross-platform, performance, and display verification remain the release checks described in the Developer Guide and `tests/test-plan.md`.
