# Combatives camera integration audit

This audit compares HMG's optional bridge with the current Combatives camera API source vendored under `HMG/referenceSRC/CombativesSRC`. The upstream repository could not be fetched in the audit environment, so the vendored current source was also inspected through its internal effect manager and renderer consumption paths.

## API surface and semantics found

Combatives camera API version 1 publishes capabilities through `CombativesCameraAPI.getApiVersion()` and `getCapabilities()`. Its public surface supports presets (`trigger`), validated custom impulses (`submitImpulse`), and owned continuous effects (`startContinuousEffect`). Capabilities independently describe pitch, yaw, roll, translation, FOV, positional falloff, continuous and preset effects, custom impulses, and client network helpers.

A custom impulse carries a namespaced effect ID; independent pitch/yaw/roll and XYZ translation; FOV contribution; duration, attack and oscillation values; decay, priority and stacking policy; and optional source entity or position. The implementation rejects missing/invalid IDs, non-finite channels, non-positive or overlong durations, invalid attack/frequency values, and effects with no processable channel. Accepted contributions stack and are saturated/clamped by Combatives before render application. FOV values become a percentage-like modifier (`value * 0.01`) in the camera controller. Positional impulses use distance falloff; the current implementation uses a fixed 32-block response rather than `CameraEffectContext.radius` for raw impulses.

Continuous effects return an ownership handle supporting active-state inspection, strength, position, enable/disable, and stop. They contribute each camera update until explicitly stopped or the Combatives manager resets. HMG retains per-shot impulses for sustained fire because the current handle can adjust strength and position but cannot update the impulse's pitch/yaw/roll vector as HMG's horizontal burst drift evolves. Replaying restrained, changing impulses therefore represents HMG's recoil model more accurately and avoids a long-lived handle. Continuous effects remain a useful future option if their channel vector becomes mutable.

Combatives also already hooks vanilla explosion packets and produces its own explosion feedback. HMG must not add a second local explosion effect without first proving that an HMG event bypasses that packet path.

## HMG findings and corrections

The previous bridge discovered only `isAvailable`, impulse construction, and submission. It assumed yaw, roll, and translation existed whenever those reflected methods loaded, and did not query API version or capabilities. It also globally discarded pending legacy recoil on every tick merely because the Combatives camera was active. That violated shot-level ownership: if a particular submission was rejected, the handler queued legacy recoil, but the next tick erased it.

The bridge now caches API version and capability discovery once, requires custom impulses and pitch for ownership, and independently gates yaw, roll, translation, and FOV builder use. A pre-discovery API degrades to the conservative pitch-only custom impulse contract. All linkage remains reflective and behind Forge optional-mod detection, so no Combatives class appears in an HMG method signature or field type.

Legacy recoil is now cleared only in the packet handler after the exact base kick is accepted. API absence, disabled camera configuration, missing required capabilities, reflection failure, validation rejection, or submission failure returns `false` to the handler and preserves the queued legacy path. Secondary punch or sustained-pressure rejection does not revoke ownership after the base shot was accepted.

Burst state resets on release after the burst window, reload, death, dimension/player replacement, and weapon switch. Single shots use pitch-dominant kick plus capability-gated restrained yaw, coupled roll, and rearward translation. Automatic fire retains deterministic burst buildup and changing drift through per-shot kick/pressure impulses, while actual crosshair displacement and delayed recovery remain owned by `HMGAimRecoilController` only for accepted shots.

## Further opportunities

- **Recommended now:** Capability-gated pitch/yaw/roll/translation custom impulses. These are implemented, cheap, driven by existing gun recoil and fire-rate data, and preserve exact-shot fallback.
- **Worth adding later:** A small, capability-gated FOV impulse for only the heaviest recoil classes. It is technically cheap but needs pack-level play testing to avoid FOV pumping and currently lacks a clear HMG weapon-class signal beyond recoil magnitude.
- **Worth adding later:** Continuous effects for sustained handling if Combatives adds mutable rotation/translation vectors, or if HMG adopts a stable neutral pressure layer. Current per-shot drift is more expressive and has simpler ownership.
- **Worth adding later:** Positional nearby-fire pressure for unusually powerful weapons. It would require an explicit, rate-limited server event and deduplication strategy; sending ordinary gunfire events would add excessive traffic.
- **Not worthwhile:** A second generic explosion/launcher effect. Combatives already consumes vanilla explosion packets, so this risks duplicate feedback. Revisit only for confirmed HMG explosions that do not reach that path.
- **Not worthwhile:** Arbitrary handling shake. It is not tied to firearm energy or HMG state and would dilute recoil feedback.
