# Diagrams

Made with [Archify](https://github.com/tt-a1i/archify) 3.2.0. Each diagram has three forms:

| file | what it is |
|---|---|
| `src/<name>.<type>.json` | the Archify source (edit this) |
| `interactive/<name>.html` | the standalone, explorable diagram: download it and open it in a browser (focus, route tracing, Live motion, export) |
| `<name>-light.svg`, `<name>-dark.svg` | static exports embedded in the README, picked by GitHub's light or dark theme |

| diagram | type | README section |
|---|---|---|
| `two-pointers` | workflow | §4 The insight |
| `thousand-machines` | sequence | §10 A thousand machines |
| `solution-map` | tree | §12 Interview round |
| `harness` | dataflow | Files |

## Regenerate

From this folder, with Archify installed (`archify.mjs` is in the installed skill's `bin/`):

```bash
node <archify>/bin/archify.mjs finalize workflow src/two-pointers.workflow.json interactive/two-pointers.html --quality showcase --json
node export.cjs interactive/two-pointers.html
```

`finalize` validates the source, renders it and runs the browser checks; `export.cjs` uses the viewer's own Export menu to write the two SVGs.
