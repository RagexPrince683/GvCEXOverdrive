# Compatibility Notes

## Combatives camera recoil

HMG-Overdrive supports optional first-person camera recoil integration with Combatives. When Combatives is installed, `Compatibility.enableCombativesRecoilIntegration` is enabled, and the Combatives camera API accepts the base shot impulse, HMG submits visual weapon recoil as named Combatives camera impulses under separate kick, punch, and sustained source IDs.

The integration keeps HMG weapon configuration as the baseline and derives camera response from the active weapon recoil value, ADS/grip recoil multiplier, and fire-rate data. Combatives owns final camera stacking, smoothing, recovery, and rendering. HMG does not change projectile direction, spread, server-side aim, networking, or third-person model behavior for this integration.

The Combatives path includes:

- distinct first-shot pitch punch;
- smaller continuous horizontal yaw drift with directional continuity;
- visible yaw recoil through Combatives yaw support;
- roll based on horizontal recoil direction;
- rearward camera translation when the Combatives camera API accepts translation impulses;
- short high-frequency punch impulses for fire-rate-sensitive camera pressure;
- burst reset after a short firing gap or weapon/world/player-state change.

If Combatives is absent, disabled, unavailable, or rejects the base kick impulse for a shot, HMG automatically uses the legacy smooth recoil path. The legacy path is cleared only after the exact shot impulse is accepted, so both recoil systems are not applied at the same time and rejected API submissions still preserve normal HMG recoil.


Verbose recoil diagnostics can be enabled with `Compatibility.enableCombativesRecoilDebug=true`. Diagnostics log each submitted source ID, rotation, translation, timing, frequency, decay type, priority, stacking mode, acceptance result, fallback decision, and weapon-state reset. Normal gameplay should leave this disabled.
