# TAPARI_STATUS.md - Momo & Chiya Empire (Tapari Tycoon)

Package: `com.tapari.tycoon` - Repo: https://github.com/NikitHamal/taparitycoon
Rule: never run Gradle locally. CI (GitHub Actions) builds debug + signed release APKs and publishes the Release `latest`.

## Done (session 1)
- [x] Gradle project (AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06, minSdk 26), wrapper files, `.github/workflows/build-release.yml`
- [x] OpenGL ES 3.0 low-poly engine (`Gfx.kt`, `GameRenderer.kt`, `Scene.kt`, `Atlas.kt`): dynamic zero-alloc batches, matrix stack, procedural shapes, sprite atlas drawn with Canvas, warm sun + hemisphere light, blob shadows, billboard bubbles / steam / sparks, ground pads with live cash countdown
- [x] Game simulation (`Game.kt`): maida/meat -> prep -> Moktu -> plates -> tables -> cash piles, Extra Jhol cauldron + angry flip-outs, Chiya & Churot camper corner (No-Camping vs Surya/Samosa upsell, mutually exclusive), VIP vloggers/bikers/tourists, 4 hireable staff roles with real hauling AI, 4 franchise locations, daily challenges, gems, prestige (franchise rebirth), boost
- [x] Audio synth (`AudioFx.kt`): generated SFX (SoundPool) + street ambience loop (traffic, horns, temple bell, bansuri flute); haptics
- [x] Save/load + offline earnings (`Persist.kt`)
- [x] Compose HUD (`Hud.kt`, `MainActivity.kt`): floating joystick, cash/gem pills, flashing EXTRA JHOL EMPTY alert, upgrade sheet, franchise map, daily challenges, settings, offline dialog

## Verified (session 1)
- CI run #1 green: debug + signed release APK published to Release `latest`
- Headless JVM harness (not committed) ran 20 simulated minutes of Game + Scene with a bot: no exceptions, full economy loop works, peak 73k verts / 85k idx (buffers 120k/260k)
- Fixed: accidental pickups when running past crates (zones shrunk, 0.3s dwell + slow-speed gate), VIP gem rate 30%

## Layout / file map
`Defs.kt` constants + layout + formatting - `Game.kt` simulation - `Scene.kt` all 3D drawing - `Gfx.kt` GL batches - `Atlas.kt` textures - `GameRenderer.kt` camera + passes - `AudioFx.kt` - `Persist.kt` - `Hud.kt` - `MainActivity.kt`

## CI log
(see bottom, updated after each run)
- Run 1 (bb71d20): success - Release `latest` published (debug 8.0MB + release 6.0MB)
- Run 2 (26b366e): success - Release `latest` updated

## Next ideas
- Persist a fixed signing keystore as a repo secret so updates install over older builds (CI currently generates a throwaway keystore each run)
- Tutorial arrows, more VIP types, per-location unique props
- Rotate the GitHub PAT used for setup (it was pasted in chat)
