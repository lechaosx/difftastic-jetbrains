# Notes for agents

## Documentation files

This project maintains four documentation files that **must be kept up to date** whenever
changes are made to the codebase, CI pipeline, or release process:

| File | Purpose | Update when |
|---|---|---|
| [`README.md`](README.md) | User-facing: what the plugin does, how to install and use it | Features added/changed, settings changed, requirements changed |
| [`CHANGELOG.md`](CHANGELOG.md) | Release history in [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format; each version's section is the plugin's What's New on JetBrains Marketplace | Any user-visible change; new `[Unreleased]` entry per change, moved to a dated version on release |
| [`MAINTAINERS.md`](MAINTAINERS.md) | Developer reference: architecture, build, CI, release process | Architecture changes, CI changes, new design decisions, release process changes |
| [`AGENTS.md`](AGENTS.md) | This file — context for coding agents | When the documentation structure itself changes |

## Keeping docs current

- When adding a feature, update `README.md` (usage section) and add a line to `CHANGELOG.md` under `[Unreleased]`.
- Write `CHANGELOG.md` entries for plugin users reading What's New: describe what changed in the IDE
  in their terms, not how it is implemented, and leave out changes they can't notice (build, CI,
  refactoring, docs).
- When changing the architecture, update the relevant section in `MAINTAINERS.md`.
- When the CI pipeline changes, update the CI/CD section in `MAINTAINERS.md`.
- On release: move `[Unreleased]` entries to a new `## [X.Y.Z] - YYYY-MM-DD` section in `CHANGELOG.md`.
- Do not leave documentation stale — a reader should be able to understand the current state
  of the project from these files without reading the code.
