# Maintainers

Technical reference for developing and releasing difftastic-jetbrains.

## Dev environment

All dependencies are in `flake.nix`. On NixOS:

```sh
nix develop        # enter shell with JDK 21, Gradle 9.5, X11 libs for runIde
gradle buildPlugin # produce build/distributions/difftastic-jetbrains-*.zip
gradle runIde      # launch a sandboxed IDE with the plugin loaded
```

The sandbox lives at `.intellijPlatform/`. Delete it to reset the sandbox IDE state.

## Architecture

```
DifftasticDiffTool      — registers as a FrameDiffTool; injects CUSTOM_DIFF_COMPUTER
DifftasticViewer        — SimpleDiffViewer subclass; owns toolbar toggles and per-viewer state;
                          clears CUSTOM_DIFF_COMPUTER on dispose so other viewers are unaffected
DifftasticRunner        — stateless; runs difft subprocess and returns DifftasticResult
DifftasticParser        — JSON → DifftasticResult via Gson
LineFragmentConverter   — DifftasticResult → List<LineFragment>
                          one fragment per chunk; aligned_lines drives insertion/deletion anchors
DifftasticSettings      — persistent settings service (binary path); binary resolution cache
DifftasticSettingsConfigurable — Settings → Tools → Difftastic UI
```

### Key design decisions

- **One `LineFragment` per difftastic chunk** — this is what gives correct scroll alignment.
  Per-line fragments cause anchor drift for modification pairs.
- **`aligned_lines` for anchors** — pure insertions/deletions use the exact lhs↔rhs mapping
  from the JSON rather than a formula. This avoids off-by-one errors.
- **Cache on `DiffRequest`, not the viewer** — the cache is stored as user data on the
  `DiffRequest` so it survives diff tool switches within the same tab. It is unbound and
  freed when the tab closes. Fragment conversion is cheap and always re-runs (e.g. when
  token filter toggles change); only the difft subprocess result is cached.
- **`CUSTOM_DIFF_COMPUTER` cleared on dispose** — ensures switching away from Difftastic
  to Side-by-side or Unified uses IntelliJ's built-in algorithm.

## CI/CD

Workflows live in `.github/workflows/` and run Gradle inside `nix develop`.

- **`ci.yml`** (pushes to `main`, pull requests) — runs `buildPlugin` and uploads the zip as an artifact.
- **`release.yml`** (tags `v*`) — builds with `pluginVersion` set from the tag (strip `v` prefix)
  and creates a GitHub Release with the zip, using the tag's `CHANGELOG.md` section as notes.

## Releasing

1. Update `CHANGELOG.md` — add a `## [X.Y.Z] - YYYY-MM-DD` section under `[Unreleased]`
2. Commit: `git commit -am "Release vX.Y.Z"`
3. Tag: `git tag vX.Y.Z && git push origin vX.Y.Z`

`release.yml` handles the rest: build → GitHub Release with the zip attached.

## IntelliJ Platform SDK

Target platform: `intellijIdea("2025.3.5")` in `build.gradle.kts`.  
Minimum version: `since-build="253"` in `plugin.xml`.  
JVM toolchain: 21 (matches the JBR bundled with IntelliJ 2025.x).

To update the target: change the version string in `build.gradle.kts` and bump
`since-build` in `plugin.xml` if the minimum supported IDE version increases.
