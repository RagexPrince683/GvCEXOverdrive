# Changelog

## HMG Overdrive first-person camera overhaul
- Replaced the experimental HMG bob waveform with an internal reproduction of the vanilla waveform that Angelica/NotFine gates for camera/hand bob modes, keeping ADS scaling and HMG ownership without re-enabling Angelica bobbing.
- Routed active HMG sight/scope magnification through Overdrive FOV inertia when the camera system is enabled, preserving legacy FOVUpdate zoom when Overdrive FOV is disabled.
- Added internal-only scope zoom transition tuning, subtle scoped breathing sway for unstabilized first-person ADS, spring-based movement lean inertia, step-weighted HMG bobbing, and stronger low-frequency explosion camera impulses.


## Unreleased

### Added

- Added Angelica camera compatibility that detects `angelica`, safely forces conflicting Angelica view bob/dynamic FOV options off when enabled, and lets HMG own first-person bob/FOV behavior.
- Added stronger HMG procedural step bob with smoothed movement-direction transitions, sprint blending, ADS bob reduction, dynamic recoil punch/wander/recovery, and heavier low-frequency explosion shake.

- Smoothed the UniMixins camera movement pass to remove WASD micro-jitter and changed explosion shake to a damped low-frequency impulse.
- Added a UniMixins-based, client-only first-person Overdrive camera controller for visual smoothing, movement tilt, custom bobbing, FOV inertia, and stacked shake impulses.
- Reduced public `ClientCamera` configuration to the supported compatibility and master switches, keeping camera/recoil tuning internal to avoid client-side recoil abuse.

### Documentation

- Refocused root and extended documentation on the actively maintained `HMG/` module, Handmade Guns Overdrive.
- Replaced GVC-centric guidance with HMG installation, dependency, compatibility, configuration, command, server, and content-pack documentation.
- Documented active `HandmadeGuns.cfg` keys and defaults from `HandmadeGunsCore`.
- Documented HMG commands `/reloadSettings` and `/hmgmanual` with permission levels and practical usage notes.
- Added a content-pack guide for `handmadeguns_Packs`, legacy pack paths, supported pack folders, resource handling, and `additionalSettings.txt`.
- Recorded HMG-specific undocumented systems discovered during the pass and remaining documentation gaps.
