# HuntR documentation

- [User Guide](UserGuide.md): command syntax and exact errors, employee history, view preservation and save failures.
- [Developer Guide](DeveloperGuide.md): immutable snapshots, checkpoints, transaction boundary and integration contract.
- Repository `tests/test-plan.md`: behavioral coverage, Java 25 validation and rendered-UI performance acceptance.

Build this site explicitly before opening a pull request:

```sh
cd docs
npm ci
npm run build
```

With dependencies already installed, `npm run build` is sufficient. The generated site is in `docs/_site` and is not
committed. The build renders the referenced PlantUML diagrams as well as the Markdown pages. Java and Graphviz
must be available for the default renderer. A headless Java/Smetana alternative used for local validation is
recorded in `tests/test-plan.md`; check the generated images because MarkBind can report success despite a
renderer subprocess failure.

The history branch and `codex/104-safe-data-saving` must both start at recorded local `master` commit
`1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`. The history branch uses the existing storage API and must pass independently.
Its owner is responsible for updating the combined user-facing explanation of memory rollback and atomic saving
and repeating the performance acceptance run after integration. Release-wide version labels remain unchanged.
