# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [0.1.1] - 2026-11-11

### Changed
- Updated for compatibility with upcoming IDE versions

## [0.1.0] - 2026-11-10

### Added
- **Difftastic** viewer in the diff tool selector, next to Side-by-side and Unified, showing
  syntax-aware diffs from [difftastic](https://github.com/Wilfred/difftastic)
- Changed tokens highlighted individually: deletions in red, insertions in green
- Toolbar toggles to show only changes of selected token types: identifiers, keywords, types,
  strings, comments and delimiters
- Switching between diff viewers in the same tab reuses the difftastic result instead of running it again
- **Settings | Tools | Difftastic** to set the `difft` binary path, with a warning when it can't be found
  (defaults to `difft` on `$PATH`)
