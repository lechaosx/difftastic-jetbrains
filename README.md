# difftastic-jetbrains

Semantic diff for JetBrains IDEs (CLion, PyCharm, IntelliJ IDEA, …) powered by
[difftastic](https://github.com/Wilfred/difftastic).

Instead of a line-by-line diff, difftastic parses both files as syntax trees and
reports which AST nodes actually changed — ignoring formatting, moved whitespace,
and other non-semantic noise.

## Requirements

- JetBrains IDE 2025.3 or later
- [`difft`](https://github.com/Wilfred/difftastic#installation) available on `$PATH`
  (or configured explicitly in Settings). The plugin reads difftastic's JSON output,
  which upstream marks unstable, so a future `difft` release may require a plugin update.

## Installation

1. Download the latest `.zip` from [Releases](https://github.com/lechaosx/difftastic-jetbrains/releases)
2. **Settings → Plugins → ⚙ → Install Plugin from Disk…**
3. Select the `.zip` — no restart required

## Usage

Open any two-panel diff view (e.g. a Git commit, a file comparison). A **Difftastic**
entry appears in the diff tool selector at the top of the diff window alongside the
standard _Side-by-side_ and _Unified_ viewers. Select it to switch.

### Token type toggles

The diff toolbar gains six toggle buttons that control which token categories are
highlighted:

| Toggle | Covers |
|---|---|
| Identifiers | Variable names, numbers, plain tokens |
| Keywords | Language keywords (`if`, `return`, `class`, …) |
| Types | Type names and annotations |
| Strings | String and text literals |
| Comments | Inline and block comments |
| Delimiters | Brackets, semicolons, commas |

**All on** (default) — every changed token is highlighted.  
**Some on** — only changes of the selected types are shown; hunks with no matching
changes disappear entirely.  
**All off** — no token highlights and no hunks shown.

This lets you iterate through a diff in passes: review only structural delimiter
changes in one pass, then identifier renames in the next, and so on.

## Configuration

**Settings → Tools → Difftastic**

| Setting | Default | Description |
|---|---|---|
| difft binary | *(auto)* | Explicit path to `difft`. Leave empty to locate it on `$PATH`. |

## Build from source

Requires Gradle 9.5 and JDK 21. On NixOS all dependencies are provided via the flake:

```sh
nix develop
gradle buildPlugin
```

The plugin zip is produced at `build/distributions/`.
