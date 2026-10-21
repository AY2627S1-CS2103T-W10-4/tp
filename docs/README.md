# HuntR documentation

- [User Guide](UserGuide.md): commands, employee history, view preservation, atomic saving and distinct backup errors.
- [Developer Guide](DeveloperGuide.md): immutable snapshots, centralized history commits, checkpoints,
  atomic replacement, supported access protections and the combined memory/disk commit boundary.
- Repository `tests/test-plan.md`: behavioral coverage, platform validation and rendered-UI performance acceptance.

Build documentation explicitly before merge; `.github/workflows/docs.yml` runs only after pushes to `master`:

```sh
cd docs
npm ci
npm run build
```

Java and Graphviz must be available to render PlantUML diagrams. Inspect the generated pages and diagrams in
`docs/_site`, which is not committed; MarkBind may report overall success even if a diagram subprocess fails.

The dependency declares `markbind-cli ^5.1.0`, with **5.1.0** locked in `package-lock.json`. The deployment workflow
uses `MarkBind/markbind-action@v2` with **^7.2.1**. This feature does not upgrade either dependency. Record the exact
installed version and build environment in `tests/test-plan.md`. The undo/redo branch owner must also verify the
combined documentation with the deployment toolchain and record its resolved version before merge.

Both feature branches start from recorded local `master` `1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`.
Merging `master` at `b67b54b538d117d8d3a2f7de0da667f8be2f9692` incorporates the undo/redo work from PR #115 into
`branch-safe-data-saving`. The combined guides describe checkpoint rollback before atomic replacement and the
successful replacement as the commit point. Repeat the full Java suite and three-session performance run with
atomic saving enabled, and build the combined documentation with both toolchains before release.

Both PRs reference #104. Keep PR #116 current with `master`, request `@sjyjoshua` as reviewer, and require green CI
and approval before merging. Close #104 only after both branches and all combined acceptance checks pass.
