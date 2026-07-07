# Changelog

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
