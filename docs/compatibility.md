# Compatibility Notes

## Combatives camera recoil

HMG-Overdrive supports optional first-person camera recoil integration with Combatives. When Combatives is installed, `Compatibility.enableCombativesRecoilIntegration` is enabled, and the Combatives camera API reports itself available, HMG submits visual weapon recoil as named Combatives camera impulses under `hmg_overdrive:weapon_recoil`.

The integration keeps HMG weapon configuration as the baseline and derives camera response from the active weapon recoil value, ADS/grip recoil multiplier, and fire-rate data. Combatives owns final camera stacking, smoothing, recovery, and rendering. HMG does not change projectile direction, spread, server-side aim, networking, or third-person model behavior for this integration.

The Combatives path includes:

- distinct first-shot pitch punch;
- smaller continuous horizontal yaw drift with directional continuity;
- roll based on horizontal recoil direction;
- rearward camera translation when the Combatives camera API accepts translation impulses;
- short high-frequency punch impulses for fire-rate-sensitive camera pressure;
- burst reset after a short firing gap or weapon/world/player-state change.

If Combatives is absent, disabled, or its camera API is unavailable, HMG automatically uses the legacy smooth recoil path. The legacy path is cleared while Combatives owns recoil so both recoil systems are not applied at the same time.
