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

## Unreleased

### Documentation

- Refocused root and extended documentation on the actively maintained `HMG/` module, Handmade Guns Overdrive.
- Replaced GVC-centric guidance with HMG installation, dependency, compatibility, configuration, command, server, and content-pack documentation.
- Documented active `HandmadeGuns.cfg` keys and defaults from `HandmadeGunsCore`.
- Documented HMG commands `/reloadSettings` and `/hmgmanual` with permission levels and practical usage notes.
- Added a content-pack guide for `handmadeguns_Packs`, legacy pack paths, supported pack folders, resource handling, and `additionalSettings.txt`.
- Recorded HMG-specific undocumented systems discovered during the pass and remaining documentation gaps.
