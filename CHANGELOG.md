# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- **Difftastic** option in the diff tool selector alongside the standard Side-by-side and Unified viewers
- Semantic, AST-based diff powered by [difftastic](https://github.com/Wilfred/difftastic)
- Correct scroll alignment via one `LineFragment` per difftastic chunk
- Exact insertion/deletion anchoring using `aligned_lines` from the JSON output
- Per-token highlights: deleted tokens shown in red, inserted tokens in green
- Token type focus toggles in the diff toolbar — enable/disable highlights for individual token categories:
  - Identifiers (`normal`)
  - Keywords
  - Types
  - Strings
  - Comments
  - Delimiters
- When no token type is selected, all token highlights are hidden (line-level blocks remain hidden too)
- Result cache tied to tab lifetime — unbound, survives switching between diff tools in the same tab, freed when the tab closes
- Configurable `difft` binary path under **Settings → Tools → Difftastic** with file browser and availability warning (auto-detected from `$PATH` by default)
- NixOS dev shell via `flake.nix` providing JDK 21 and Gradle with all required X11 libraries for `runIde`
- GitHub Actions: build on every push, publish a GitHub Release with the plugin zip on version tag
