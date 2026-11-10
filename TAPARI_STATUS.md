# TAPARI_STATUS.md - Momo & Chiya Empire (Tapari Tycoon)

Package: `com.tapari.tycoon` - Repo: https://github.com/NikitHamal/taparitycoon
Rule: never run Gradle locally. CI (GitHub Actions) builds ONE signed release APK and publishes Release `latest`.

## CI / signing (session 2)
- Workflow `.github/workflows/build-release.yml` triggers only on pushes to `main` that touch `app/**`, `gradle/**`, `keystore/**`, root gradle files or the workflow itself. Docs / status / README pushes and PRs never build.
- Only `assembleRelease` runs. Output: `TapariTycoon-release.apk` (Release tag `latest`).
- Signing uses the committed public keystore `keystore/tapari-release.p12` (alias `tapari`, password `tapari123`), identical in every build so updates install over old ones. Play re-signs in production, so it is intentionally public.

## Session 1 (done)
- OpenGL ES 3.0 low-poly engine (`Gfx.kt`, `GameRenderer.kt`, `Atlas.kt`), game sim (`Game.kt`), audio synth (`AudioFx.kt`), save/offline (`Persist.kt`), Compose HUD.

## Session 2 (this overhaul)
- ROOT CAUSE of "characters not visible / far off": `person()` never reset the matrix, so every character was drawn offset by the last station's transform. New `Rig.person()` always starts with `loadId()`.
- `Rig.kt`: articulated characters (thigh/knee/shin, shoulder/elbow/hand, torso lean + twist, neck/head look-around, blink, eyebrows + mouth moods, hair styles, Dhaka topi / chef cap / bandana / helmet / sun hat / hoodie / cap, apron, tie, backpack, camera, glasses, scarf/dupatta, biker jacket, ring-light phone). Poses: walk, carry, eat, cheer, angry, work, phone, yell. Customers yell with raised arm + open mouth when they want Extra Jhol, cheer after paying, stomp when angry.
- `City.kt`: world builder. Footpath stage with TO-LET shutter facade, awning, bulbs, gas cylinder, FOR SALE board; buying the shop animates walls rising, shutter rolling up, signboard dropping in. Street with sidewalk, kerb, zebra, lamps, poles + wires, trees, moving buses/cars/bikes/trucks, pedestrians, neighbour shopfronts, skyline, Himalayas, Boudha-style stupa, pagoda, prayer flags. Per-location themes (Baneshwor, New Road, Jhamsikhel rooftop, night Global plaza).
- Layout is ~1.3x bigger (`Defs.kt` `Layout`), new station id 17 = `BUILDING` pad. Tables 4-6, Chiya kettle and benches only appear after the building is bought (`Station.avail`). Locations 2-4 start with the building owned.
- Pads now need a 0.4s still dwell before paying and a slow speed (no more accidental spending while running). Pickup dwell kept.
- Speech bubbles are compact icon bubbles (vector-drawn in `Atlas.kt`), max 7 on screen, priority scored (jhol/angry/VIP first), stacked max 2 levels so crowds stay readable.
- Guidance arrow (`Scene.drawGuide`) over the next useful station; HUD objective card with live progress (`Game.computeGoal`), tourist-bus Rush Hour random event.
- `Icons.kt`: custom Canvas icon set (no emoji anywhere). `Hud.kt`: cleaner HUD (cash/gem pills, objective, jhol gauge only when it matters, carry chip, bottom dock, pause menu), floating joystick whose base follows the thumb with a dead zone. `Screens.kt`: loading screen + main menu (live orbiting 3D shop behind it) + how-to-play. `MainActivity.kt`: LOADING -> MENU -> PLAYING; loading warms save load, audio synth and GL (shaders + atlas) behind the splash.
- Camera: closer follow camera with look-ahead, orbit in menu.

## Verified
- Run for 1cc4bd3 (CI-only change): green, signed release APK published with the committed keystore.
- Run for 060bc24 (full overhaul) and 6ce4a6a (polish): green. Release APK signature verified to contain the committed keystore certificate (SHA-256 D1:98:0D:0A:...:AD:FC).
- Rig geometry was software-rendered offline (Python port) to sanity-check orientation, hats, faces, poses.

## CI log
- Run 1 (bb71d20): success (old workflow, debug + release)
- Run 2 (26b366e): success
- Run 3 (1cc4bd3): success - release-only, committed keystore
- Run 4 (060bc24): success - overhaul
- Run 5 (6ce4a6a): success - polish, load-shedding event

## Next ideas
- Per-location unique props, more VIP types, tutorial voice lines, achievements, upgrade tiers for Moktu visuals
- Rotate the GitHub PAT that was pasted in chat
