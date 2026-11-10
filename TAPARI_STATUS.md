# TAPARI_STATUS.md - Momo & Chiya Empire (Tapari Tycoon)

Package: `com.tapari.tycoon` - Repo: https://github.com/NikitHamal/taparitycoon
Rule: never run Gradle locally. CI (GitHub Actions) builds ONE signed release APK and publishes Release `latest`.

## CI / signing
- Workflow `.github/workflows/build-release.yml` triggers on pushes to `main` touching `app/**`, gradle files, keystore or the workflow. Docs-only pushes do not build.
- Output: `TapariTycoon-release.apk` (Release tag `latest`), signed with committed public keystore `keystore/tapari-release.p12`.

## Session 1-2 (done)
- OpenGL ES 3.0 low-poly engine (`Gfx.kt`, `GameRenderer.kt`, `Atlas.kt`), sim (`Game.kt`), audio synth (`AudioFx.kt`), save/offline (`Persist.kt`), Compose HUD, articulated characters (`Rig.kt`), city (`City.kt`), loading/menu (`Screens.kt`).

## Session 3 (this overhaul) - icons, font, polish, gameplay
- ICONS: every emoji-like hand-drawn icon is gone. `GlyphData.kt` (generated) holds vector path data from Google Material Icons (Apache 2.0) and game-icons.net (CC BY 3.0, credited in Pause menu). `Icons.kt`: `Ic` enum -> tinted single-colour glyphs, `GIcon`, `GBadge`, `GlyphLib` (parses paths, shared by Compose and the GL atlas). `Atlas.kt` draws station/item icons as outlined glyph stickers and speech bubbles as coloured badges with glyphs.
- FONT: Poppins wired into Compose (`Hud2.kt` `Poppins` + `poppinsTypography()`, applied in `TapariTheme`) and into the GL atlas digits/labels (`Atlas.init(ctx)`).
- LAUNCHER: new adaptive icon (dumpling glyph) in `mipmap-anydpi-v26`.
- 3D: 4x MSAA config chooser (`Msaa.kt`), improved vertex lighting (wrap diffuse, grounded AO, rim, saturation), squash/stretch + dash lean in `Rig`, combo-coloured player ring + orbiting stars, dash speed lines + FOV punch, patience bars over customers, rattling Moktu lid + flickering fire, confetti / ripple / floating label particles, golden momo pickup with light beam.
- GAMEPLAY: combo streak (x multiplier, perfect-service bonus, milestone gems), Dash button (cooldown ring), Golden Momo random pickup (cash / gems / Chiya Rush / double cash), boss level + XP with gem per level, 13 achievements, Lucky Spin wheel (free every 15 min, or 2 gems), 7-day login streak, 3 new upgrades (Cash Magnet, Combo Master, Dash Boots), double-cash timer, vignette overlay. New SFX: dash, combo, gold, spin, achievement.
- SAVE: all new counters persisted (`Persist.kt`).

## CI log
- Runs 1-5: success (sessions 1-2); d03c269 success.
- Session 3 runs: see bottom (updated after each push)

## Next ideas
- Per-location unique props, more VIP types, tutorial voice lines, Moktu visual tiers
- Rotate the GitHub PAT that was pasted in chat
