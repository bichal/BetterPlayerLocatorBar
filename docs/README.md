# docs

| Path | Published to | When |
|---|---|---|
| `../README.md` | GitHub, and the Modrinth and CurseForge descriptions (minus `<!-- publish:off -->` blocks) | Modrinth: on each `v*` tag (README pushes only render it). CurseForge: paste the `descriptions` artifact (no API exists) |
| `changelogs/<version>.md` | Changelog of that version on Modrinth, CurseForge and the GitHub release | On the `v<version>` tag (CI fails if the file is missing) |
| `media/` | Images used by the README (made absolute for Modrinth/CurseForge) | With the README |

Publishing is done by `panzer-build-logic/publishing` (see its README): the same tool and workflows for every mod.

## Releasing a new version

1. Copy the latest `changelogs/<version>.md` to `changelogs/<new version>.md` and rewrite it, keeping the
   same structure: a summary paragraph, `---`, `## Key Features & Changes` with `* **Name**: description` bullets.
2. Set `version` under `[mod]` in `mod.stonecutter.properties.toml` to the new version.
3. Check the dry run of the last push (`release-preview` artifact): pages, files and tags as they will be published.
4. Commit and push, then tag: `../panzer-build-logic/panzer release bplb --push` (checks the changelog and that
   the shared files are in sync, then pushes `v<new version>`), or `git tag v<new version> && git push origin v<new version>`.
   The Mods workflow of panzer-build-logic (*action: release*) does the same from GitHub.

## Previewing without publishing

- Every push: the **release-preview** artifact of the "CI" run (open `preview.html`).
- Locally: `../panzer-build-logic/panzer publish preview --mod . --out /tmp/preview` (after `./gradlew buildAndCollect`).
- On demand: *Actions → CI → Run workflow* (dry run by default).
