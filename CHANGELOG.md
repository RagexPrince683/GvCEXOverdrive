# Changelog

## Unreleased

### Added

- Smoothed the UniMixins camera movement pass to remove WASD micro-jitter and changed explosion shake to a damped low-frequency impulse.
- Added a UniMixins-based, client-only first-person Overdrive camera controller for visual smoothing, movement tilt, custom bobbing, FOV inertia, and stacked shake impulses.
- Added configurable `ClientCamera` options in `HandmadeGuns.cfg`, plus visual recoil shake integration for existing HMG recoil packets.

### Documentation

- Refocused root and extended documentation on the actively maintained `HMG/` module, Handmade Guns Overdrive.
- Replaced GVC-centric guidance with HMG installation, dependency, compatibility, configuration, command, server, and content-pack documentation.
- Documented active `HandmadeGuns.cfg` keys and defaults from `HandmadeGunsCore`.
- Documented HMG commands `/reloadSettings` and `/hmgmanual` with permission levels and practical usage notes.
- Added a content-pack guide for `handmadeguns_Packs`, legacy pack paths, supported pack folders, resource handling, and `additionalSettings.txt`.
- Recorded HMG-specific undocumented systems discovered during the pass and remaining documentation gaps.
