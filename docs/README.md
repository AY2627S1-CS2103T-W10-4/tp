# HuntR documentation

- [Product website](index.md): final product overview and user-interface description.
- [User Guide](UserGuide.md): installation, command formats, examples, behaviour and known limitations.
- [Developer Guide](DeveloperGuide.md): architecture, implementation details, requirements and manual testing.
- [About Us](AboutUs.md): project team and responsibilities.

To build the documentation locally:

```sh
cd docs
npm ci
npm run build
```

Java and Graphviz are required to render the PlantUML diagrams. Inspect the generated pages and diagrams in
`docs/_site`, which is not committed. GitHub Actions also builds and publishes the documentation from `master`.
