# Filter and sort beta test — 2026-11-02

Task: [#236](https://github.com/AY2627S1-CS2103T-W10-4/tp/issues/236).

## Baseline and method

- Official `v1.5` release asset `huntr.jar`, SHA-256
  `cad2a7511498ce521c37632a1ffcca0e7061a803f86936202120ffda8be1a7b0`.
- Matching UG: `docs/UserGuide.md` at tag `v1.5` (`ac6e5e7b`).
- Fix branch started from upstream master `4898ddcb`.
- Windows, Java 25.0.4.1. An independent command runner used the release JAR's actual
  `LogicManager`, parser, model and JSON storage with disposable files. It did not
  substitute test doubles or use the user's roster. GUI smoke tests separately
  confirmed the reported Unicode failure and the packaged fix.
- The runner checked result IDs, exact documented error messages including usage,
  current view, stored records, undo/redo availability, JSON bytes and read-only
  file modification times. Each table row represents several concrete commands.

## Main fixture

Employees are stored in this order. All are active except E8; all have valid phone
numbers and email addresses and no manager.

| ID | Name | Department | Role |
| --- | --- | --- | --- |
| E10 | Zoe Tan | Sales | Manager |
| E2 | John Tan | Engineering | Software Engineer (Backend) |
| E0123 | John Lee | Sales | Manager |
| E4 | Émile Roux | Finance | Manager |
| E5 | Alice Lim | Human Resources | HR Executive |
| E6 | Ravi s/o Kumar | R&D, Asia | Engineer (Backend) |
| E7 | Mary-Ann O'Brien | Engineering | Engineering Manager |
| E8 (archived) | John Archived | Engineering | Manager |

## Coverage and observations

Unless noted, all results agreed with the v1.5 UG in both release and fixed JARs.

| Area | Inputs and expected observations |
| --- | --- |
| Every filter example | `filter d/Engineering` → E2,E7; `filter r/Manager` → E10,E0123,E4,E7; `filter d/Engineering r/Manager` → E7; `filter n/John d/Sales` → E0123; `filter d/Sales d/Finance r/Manager` → E10,E0123,E4; `filter id/E0123` → E0123. |
| Filter explanations and tip | `d/engineering`, `d/Resources`, `d/Engineer` (no match), `d/R&D`, `r/Backend`, `r/(Backend)`, and reversed words `r/Engineer Software`; Engineering filter followed by `sort name` → E2,E7. |
| AND/OR and repeated fields | Repeated IDs, names and roles; duplicate values; all four criteria together; reordered prefixes; `r/Manager r/Analyst` versus `r/Manager Analyst`. Only whole words match. |
| Name punctuation and accents | `n/Mary Ann`, `n/O'Brien`, `n/Ravi s/o Kumar`; `n/ÉMILE`, `n/Emile` and decomposed `n/Émile`. |
| View replacement | `find John` then Finance filter → E4; archived list then Engineering filter → E2,E7; `list` resets filtering and sorting. |
| No matches | `filter id/UNKNOWN` → empty view and `0 employees listed!`. |
| Every filter documented error | `filter`, `filter Engineering`, unknown `e/` and `x/`, empty `d/`, `n/`, `r/`, `d/@@@`, `id/E 1`. Exact message plus usage; prior filtered/sorted view, records, disk and history unchanged. |
| Additional invalid filter inputs | Preamble with valid prefix; unknown `m/` and uppercase `D/`; punctuation `---` and `()`; empty repeated criterion; blank ID and 21-character ID. Rejected without state changes. |
| Every sort example | `sort name` → E5,E4,E0123,E2,E7,E6,E10; `sort department desc` → E10,E0123,E6,E5,E4,E2,E7; `sort role asc` → E6,E7,E5,E4,E10,E0123,E2. |
| Sort fields and direction | All four fields in both directions; mixed-case `sort ID DeSc`; default ascending. Natural numeric order checked separately with E1,E0002,E10, Agent 1/2/10 and Level 1/2/10. |
| Sort ties | Separate fixture: E10 has Émile/Sáles/Mánager; E2 has emile/sales/manager. Name, department and role each return E2,E10 in **both** directions. |
| Every sort documented error | `sort`, `sort name asc desc`, `sort salary`, `sort salary desc`, `sort name down`; also a trailing unknown parameter. Exact message plus usage and no state changes. |
| Filter/sort/find/archive composition | Sort then filter, filter then sort, sort then find; archived list can be sorted; normal list and archived list reset stored order. Archived records never leak into ordinary filters. |
| Archive and undo/redo | Engineering filter + descending name sort; archive E7 → E2; undo → E7,E2. Change sort and filter while redo is available; redo still succeeds and respects the current view. Unarchiving a nonmatching employee keeps it hidden. |
| Edits and sort | Under ID sort, rename E2 to Zara Tan: all active employees appear, still sorted by ID. Name sort then undo/redo moves E2 appropriately. Edit intentionally resets filtering, as the edit section documents. |
| Delete and history | Reapply Engineering filter with name sort; delete E7 → E2; undo → E7,E2; redo → E2. |
| Add and history | Add A1/Adam Tan under name sort; filter resets to all active, A1 sorts first; undo removes A1 and redo restores the same order. |
| Persistence and restart | Queries leave JSON bytes and mtime unchanged; data-changing operations persist the complete roster. Reconstructing the model from JSON restores active employees in stored order, with no saved filter, comparator or undo history. |
| Empty and boundary data | Empty roster accepts filter and sort. One-character `_` and 20-character IDs match, including case variants. Department and role values at 100 characters filter correctly. Leading/trailing spaces accepted. |
| Unicode case matching | Fixture U1 uses Yılmaz in name/department/role, U2 uses ΟΣ. Filtering each field with YILMAZ or οσ misses the employee in v1.5: **#255, fixed**. |
| Symbol-only invalid criteria | Each of `n/`, `d/`, `r/` with `&`, `'` or isolated U+093E is accepted. First such query replaces the earlier view: **#256, Low, document under Known issues**. |

## Results and triage

The runner performed **690 assertions** against each JAR. The official release had
16 failing assertions: six Unicode misses (#255), nine accepted symbol-only
commands and one resulting view change (#256). The corrected package removed all
six Unicode failures. The ten assertions associated with #256 remain an explicit
known limitation; this matrix is not reported as entirely passing.

- [#255](https://github.com/AY2627S1-CS2103T-W10-4/tp/issues/255): **type.Bug,
  severity.Medium**, assigned to stephen-118; fix in v1.6. The filter predicate
  compares normalized whole words with Java's case-insensitive comparator. Shared
  search tokenization and the separate `find` fix in #248 are untouched.
- [#256](https://github.com/AY2627S1-CS2103T-W10-4/tp/issues/256): **type.Bug,
  severity.Low**, assigned to stephen-118; document a workaround in the UG under
  Known issues, as #236 requires for minor bugs. No production change for this case.
- No new sort defect was found in the tested cases.

GUI checks used separate data directories. In v1.5, `filter n/YILMAZ` displayed
`0 employees listed!` despite U1 being present. With the fixed package, it displayed
U1 and `1 employee listed!`; `filter d/οσ` displayed U2 and the same singular
feedback. The two GUI data files retained the original fixture's SHA-256 after exit.

## Regression validation and freeze accounting

- Three new regression tests failed on the original production code, then passed
  with the fix. They cover all three text fields, sigma variants, combined and
  repeated criteria, negative whole-word/missing-word cases, archive exclusion,
  current sort order, unchanged stored order and no new undo step.
- `./gradlew check shadowJar`: **BUILD SUCCESSFUL**. JUnit reported 846 tests,
  17 skipped, zero failures and zero errors (829 executed successfully).
- Own v1.5 functional Java baseline, conservatively estimated by `git blame`
  using both Stephen account author identities: 843 nonblank/noncomment lines
  (1,036 attributed lines including comments/blank lines). `max(15% × 843, 150)`
  permits 150 lines. This deliberately does not assume extra allowance from resources.
- This fix changes **3 added / 2 deleted** lines under `src/main`, counting imports.
  Including the 9 changed lines in the author's open v1.6 PRs #251 and #252 gives
  **14 changed lines**, below the 150-line allowance. Tests and documentation are exempt.

Original-author review, branch synchronization and green CI remain PR merge gates;
this test report does not substitute for reviewer approval.
