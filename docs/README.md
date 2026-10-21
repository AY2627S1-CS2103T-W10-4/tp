# HuntR documentation

- [User Guide](UserGuide.md): automatic saving, supported local files and distinct backup errors.
- [Developer Guide](DeveloperGuide.md#storage-component): atomic replacement, access protections and the save boundary.
- Repository `tests/test-plan.md`: focused failure tests, validation evidence and the combined release gate for #104.

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

This branch, `branch-safe-data-saving`, and `branch-undo-redo` start from recorded local `master`
`1d8b4b5eaeae6b1a102f92dd81a7befb18221ecb`. This standalone branch protects the last stored employee file during
handled save failures; it does not implement in-memory rollback or history. Both branches must pass independently.
After integration, the undo/redo owner must reconcile the guides' memory/disk commit boundary, build the combined
documentation with both toolchains and repeat the specified three-session performance run with atomic saving.

Both PRs must reference #104. Update them against current `master`, request `@sjyjoshua` as reviewer, and require
green CI and approval before merging. Close #104 only after both branches and all combined acceptance checks pass.
