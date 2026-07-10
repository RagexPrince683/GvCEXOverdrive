# Compatibility Notes

## Combatives camera and aim recoil

HMG-Overdrive supports an optional two-layer first-person recoil integration with Combatives. When Combatives is installed, `Compatibility.enableCombativesRecoilIntegration` is enabled, and the Combatives camera API accepts the base shot impulse, HMG submits visual weapon recoil as named Combatives camera impulses under separate kick, punch, and sustained source IDs.

Accepted Combatives shots also use `Compatibility.enableCombativesAimRecoilIntegration` to apply actual client aim recoil. This aim controller mutates only the local first-person player's `rotationPitch` and `rotationYaw`, so the centered vanilla crosshair, ray traces, projectile direction, and normal client-to-server rotation sync naturally follow the displaced look direction. Combatives impulses remain visual render offsets; they do not by themselves change player aim.

The Combatives path includes:

- distinct first-shot pitch punch;
- smaller continuous horizontal yaw drift with directional continuity;
- persistent pitch climb and conservative yaw drift through the HMG aim-recoil controller;
- visible transient yaw recoil through Combatives yaw support;
- roll based on horizontal recoil direction;
- rearward camera translation when the Combatives camera API accepts translation impulses;
- short high-frequency punch impulses for fire-rate-sensitive camera pressure;
- burst reset after a short firing gap or weapon/world/player-state change.

If Combatives is absent, disabled, unavailable, or rejects the base kick impulse for a shot, HMG automatically uses the legacy smooth recoil path. `RenderTickSmoothing.addSmoothRecoil(...)` and `applySmoothRecoil(...)` are the legacy owner that mutates real player pitch/yaw, so the new aim controller is only invoked after Combatives accepts the visual shot. The legacy path is cleared only after the exact shot impulse is accepted, so both aim owners are not applied at the same time and rejected API submissions still preserve normal HMG recoil.


Verbose recoil diagnostics can be enabled with `Compatibility.enableCombativesRecoilDebug=true`. Diagnostics log each submitted source ID, rotation, translation, timing, frequency, decay type, priority, stacking mode, acceptance result, fallback decision, weapon-state reset, aim recoil shot additions, pending and accumulated pitch/yaw, per-tick application, delayed recovery, detected player mouse deltas, weapon key, and whether new aim or legacy ownership is active. Normal gameplay should leave this disabled.

Aim recoil applies most per-shot displacement over a few client ticks, waits for `combativesAimRecoilRecoveryDelayMs` after the last accepted shot, and then subtracts only the controller-owned recoil contribution. Mouse movement is preserved because recovery is contribution-based rather than locked to a pre-burst view angle; pulling down or steering into the recoil reduces the remaining recoverable contribution instead of causing a later snap back to an obsolete target.
