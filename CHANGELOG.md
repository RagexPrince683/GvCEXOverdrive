# Changelog

## HMG semantic version and automatic build numbers

- Added `HMG/version.properties` with manually managed `mod_version` and Gradle-managed `build_number` values.
- Updated the HMG Gradle build to compute `mod_version.build_number`, expose `modVersion`, `buildNumber`, and `fullVersion`, and apply the computed version to generated jars.
- Added production-build-only build number increments with rollback on failed builds so failed packaging attempts do not consume version numbers.
- Documented the release workflow so future releases only require editing `mod_version`.

## HMG → Combatives aim recoil ownership

- Added a dedicated client-side HMG aim-recoil controller for accepted Combatives shots so sustained fire now changes actual local pitch/yaw instead of relying only on short-lived visual camera impulses.
- Added contribution-based recoil recovery, burst accumulation caps, deterministic horizontal drift, mouse counter-recoil accounting, and safe reset behavior for invalid players, death, and dimension changes.
- Tuned Combatives visual recoil to remain a punch/roll/translation layer with reduced sustained pitch so persistent aim climb is owned by the HMG aim controller.
- Documented the actual aim recoil, visual recoil, delayed recovery, diagnostics, configuration, and legacy fallback split.

## HMG Combatives Camera Recoil Integration

- Added optional Combatives camera recoil integration for HMG first-person weapon fire.
- Routed compatible recoil through Combatives camera impulses with dynamic first-shot punch, sustained-fire pressure, horizontal drift continuity, and burst reset/recovery behavior.
- Preserved HMG legacy recoil as the fallback when Combatives is absent, disabled, or its camera API is unavailable.
- Added `Compatibility.enableCombativesRecoilIntegration` with a default of `true`.
- Corrected Combatives recoil ownership so legacy recoil is only suppressed after a shot impulse is accepted, strengthened the independent base pitch kick for single shots and shotguns, split kick/punch/sustained source IDs, and documented full yaw recoil support.
- Added `Compatibility.enableCombativesRecoilDebug` diagnostics for impulse submission, fallback, and weapon-state reset investigation.

## Gate spam logging behind debug config

- Added `Logging.enableDebugLogging` for verbose HMG startup/content-pack diagnostics.
- Gated content-pack resource confirmations, script confirmations, recipe success logs, and registration timing summaries behind debug logging while preserving errors, warnings, and concise registration-complete summaries by default.
- Updated configuration and server administration documentation for the quieter default logging behavior.

## Gun Smithing Table ore dictionary recipes

- Reworked Gun Smithing Table recipe ingredients to store exact-stack and ore-dictionary requirements explicitly instead of converting ore entries into a preview stack.
- Added deterministic shared inventory allocation for Gun Smithing Table GUI checks, gun crafting, and ammunition crafting so mixed-mod ore equivalents are validated and consumed consistently server-side.
- Added debug-only diagnostics and a development verification helper for exact, ore dictionary, mixed-stack, wildcard/metadata, late-registration, failed-validation, and no-double-count allocation scenarios.
- Documented ore dictionary pack recipe syntax for content authors, including optional required amounts such as `ore:ingotCopper:5`.

## Unreleased

### Documentation

- Refocused root and extended documentation on the actively maintained `HMG/` module, Handmade Guns Overdrive.
- Replaced GVC-centric guidance with HMG installation, dependency, compatibility, configuration, command, server, and content-pack documentation.
- Documented active `HandmadeGuns.cfg` keys and defaults from `HandmadeGunsCore`.
- Documented HMG commands `/reloadSettings` and `/hmgmanual` with permission levels and practical usage notes.
- Added a content-pack guide for `handmadeguns_Packs`, legacy pack paths, supported pack folders, resource handling, and `additionalSettings.txt`.
- Recorded HMG-specific undocumented systems discovered during the pass and remaining documentation gaps.

## BackTools HMG 3D back rendering

- Added a client-side BackTools compatibility bridge that reuses HMG Overdrive gun item renderers for guns shown on a player back.
- Preserved BackTools legacy back rendering for vanilla and non-HMG items, and safely falls back when a gun renderer is missing or fails.
- Added throttled diagnostic logging for custom back-render decisions and failures.

## BackTools HMG back-render stability follow-up

- Fixed HMG BackTools back rendering to copy and render only BackTools' remembered stack, skipping the back render when it matches the currently held stack.
- Removed render-time mutation of BackTools' stored item map to avoid current-weapon substitution and flicker.
- Adjusted the global back-mounted HMG gun pose so models lie flatter against the player back, and disabled culling only inside the isolated custom render path.

## BackTools HMG held-gun suppression and roll fix

- Suppressed HMG back rendering whenever the player is currently holding any HMG gun, preventing held weapons from appearing on the back.
- Restored the scoped BackTools legacy-icon suppression while HMG custom back rendering is active so only the 3D model appears after switching away from HMG guns.
- Added a final back-gun roll adjustment so the grip/bottom points downward while preserving the back-plane alignment and diagonal barrel pose.
