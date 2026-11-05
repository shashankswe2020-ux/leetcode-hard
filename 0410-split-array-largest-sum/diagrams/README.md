# Diagrams

Made with [Archify](https://github.com/tt-a1i/archify). Each diagram has three forms:

| file | what it is |
|---|---|
| `src/<name>.<type>.json` | the Archify source (edit this) |
| `interactive/<name>.html` | the standalone, explorable diagram: download it and open it in a browser (focus, route tracing, Live motion, export) |
| `<name>-light.svg`, `<name>-dark.svg` | static exports embedded in the README, picked by GitHub's light or dark theme |

| diagram | type | README section |
|---|---|---|
| `greedy-check` | workflow | §4 The insight |
| `binary-search` | workflow | §5 Write it |
| `harness` | dataflow | §6 Why it can't be wrong |
| `thousand-machines` | sequence | §9 A thousand machines |
| `solution-map` | tree | §11 Interview round |

## Regenerate

From this folder, with Archify installed (`archify.mjs` is in the installed skill's `bin/`):

```bash
node <archify>/bin/archify.mjs finalize workflow src/binary-search.workflow.json interactive/binary-search.html --quality showcase --json
node export.cjs interactive/binary-search.html
```

`finalize` validates the source, renders it and runs the browser checks; `export.cjs` uses the viewer's own Export menu to write the two SVGs.
