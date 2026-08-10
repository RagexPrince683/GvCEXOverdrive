# Changelog

## Add crafting-based gun skin removal

- Add a dynamic shapeless recipe that removes an applied gun skin when its gun is
  crafted with the same stable-ID skin item or with one dye.
- Accept vanilla dyes and modded dyes using standard Forge `dye*` Ore Dictionary
  entries while rejecting different skins and unrelated extra ingredients.
- Copy the complete gun stack and remove only the root `GunSkin` NBT value so all
  ammunition, attachment, damage, naming, and firing state remains intact.

## Fix Gun Smithing Table crafting and add NEI recipe support

- Deliver Gun Smithing Table outputs immediately from the server, drop only an
  insertion remainder, and explicitly synchronize the player and open containers.
- Serialize each inventory transaction so rapid requests cannot consume the same
  ingredients twice, and reject requests unless the smithing container is open.
- Add optional NEI integration with a dedicated Gun Smithing Table category,
  table catalyst, distinct duplicate-output recipes, and cycling ore alternatives.
- Compile against NEI's development API without packaging it as an HMG runtime
  dependency, and document recipe discovery for content-pack authors.

## Gun weight mouse sensitivity

- Reduce first-person mouse sensitivity while a unified HMG gun is held, using the
  gun definition's existing `Weight` value and a clamped weight curve.
- Combine gun-weight sensitivity with the existing ADS zoom adjustment for the same
  render camera update, then restore the user's exact configured sensitivity.
- Leave GUI input, unarmed/non-HMG input, placed guns, and vehicle cameras unchanged.

## Fix M16A4 skinned transparent reticle group selection

- Associate the M16A4 red-dot reticle marker with its actual `mat4`,
  `mat4reticle`, and `mat4reticlePlate` MQO object family instead of the
  nonexistent `mat41` family.
- Make model-group discovery color- and depth-write-free so initialization can
  never turn a transparent reticle plate into an invisible depth occluder.
- Preserve the existing isolated gun-skin housing overlay and depth-tested,
  original-texture reticle pass.

## Fix HMG transparent sight reticle rendering

- Isolate the reticle and stencil-plate pass from gun-skin OpenGL state, explicitly
  restoring normal alpha blending and `GL_LEQUAL` depth testing for both skinned and
  unskinned guns.
- Keep reticle-plate generation stencil-only by disabling depth writes, then render
  the reticle against both that stencil and the existing opaque scene depth so gun
  bodies and optic housings occlude it without making transparent lenses opaque.
- Restore the renderer's incoming pass after rendering reticle children instead of
  leaving the shared pass selector set to the transparent pass.

## Fix HMG sight housing gun skin rendering

- Apply universal gun skins to gun-owned iron-sight, red-dot, and scope housing parts
  even when their model definitions mark them as attachment parts, while continuing
  to exclude unrelated attachments, bullets, lights, lasers, and reticle/plate parts.
- Mask the overlay against the opaque base render with an exact depth comparison, so
  transparent and semi-transparent lens pixels remain on their original render path.
- Remove polygon offset from the overlay pass because the repeated geometry now uses
  the opaque pass's matching depth without writing new depth.

## Fix universal HMG gun skins

- Replace target-pair recipe generation with one dynamic recipe accepting exactly one
  `HMGItem_Unified_Guns` stack and one valid skin in either grid order.
- Preserve the complete input gun stack while setting or replacing only its `GunSkin`
  NBT value, allowing loaded, damaged, attached, named, and previously skinned guns.
- Remove the gun-name map, target matching, target warnings, and gun-specific AK47 and
  PKM development skins; legacy `SkinTarget` lines are parsed and deliberately ignored.
- Resolve overlays solely from the skin ID on each rendered gun stack, keeping skins
  instance-local and universal across gun models while excluding attachment parts.
- Update the content-pack documentation and universal `ar_sample_skin` definition.

## Fix HMG gun skin crafting with concrete recipes

- Track every final unified gun object by its pack-declared `GunName`, including
  reused registered guns, and generate one exact-object crafting recipe for each
  valid gun/skin target pair during Forge initialization.
- Preserve the input gun stack and all existing state while applying `GunSkin`, and
  report recipe totals plus every unresolved target once during startup.
- Document that skin crafting is resolved after pack loading without crafting-time
  Forge registry identifier lookups.
- Add development pack skin definitions for the real `AK47` and newer-pack `PKM`
  declarations so concrete recipe registration can be exercised beyond `AR_sample`.

## Fix HMG gun skin crafting identity matching

- Match skin targets against each gun item's actual Forge unique identifier instead
  of reverse-looking-up the target, with bare HMG `GunName` targets preferred and
  legacy fully-qualified targets retained.
- Trim and reject empty target fields, validate skin targets once after pack loading,
  and document the canonical content-pack identifier format.
- Update the Addfixing sample to target `AR_sample` by its gun declaration identifier.

## Fix the HMG gun skin pipeline

- Corrected the Addfixing sample so its inventory icon, exact `HandmadeGuns:AR_sample`
  registry target, and model texture all resolve through existing pack resources.
- Fixed the sample's root failure: `SkinTexture` named an overlay PNG that was never
  included in Addfixing, so the loader disabled the overlay before rendering.
- Moved missing-resource fallback to a cached client render-time check instead of
  storing client resource state on shared skin metadata.
- Standardized compatibility checks on exact Forge registry identifiers and kept
  texture-path construction common/server safe.
- Reset the legacy overlay pass color to white while retaining isolated blend, depth,
  and polygon-offset state.
- Resolve each exact `SkinTarget` to its registered gun `Item` for crafting and
  rendering compatibility, so damage and per-stack gun state never affect matching.
- Restore the OpenGL current-color state after each transparent overlay pass.

## Data-driven gun skins

- Added ordinary content-pack gun skin items with stable identifiers, cached overlay resources, and per-skin compatible gun registry identifiers.
- Added one generic shapeless skin application recipe that copies the gun and changes only its `GunSkin` NBT value.
- Added transparent base-model overlay rendering to the legacy and current unified gun renderers, with isolated fixed-function OpenGL state.
- Documented the skin pack keys, texture layout, crafting behavior, and an Addfixing attachment template.

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

## BackTools HMG back-gun transform correction

- Restored the working back-plane alignment for HMG back guns and moved the grip/bottom roll before the diagonal direction rotation so the model stays flat against the player's back.

## (5f21dfc Update Combatives camera recoil integration)

- Audited HMG recoil against the current Combatives camera API and documented its capabilities, validation, render semantics, continuous-effect tradeoffs, and future firearm integration opportunities.
- Added cached API-version and capability discovery with conservative degradation for missing or older optional camera surfaces.
- Fixed exact-shot recoil ownership so an active Combatives installation can no longer erase legacy recoil queued after a rejected submission.
- Added capability-gated yaw, roll, translation, and FOV construction plus burst cleanup on trigger release and reload, while retaining existing death, dimension, and weapon-switch cleanup.

## Fix HMG skin rendering across item views

- Rendered universal gun-skin overlays during HMG's guaranteed opaque model pass so they appear once in first person, third person, dropped-item, and model-inventory rendering without relying on Forge to provide item render pass 1.
- Preserved per-stack skin selection and the exclusions for attachment and bullet model parts; legacy scripted guns remain supported when their scripts use the renderer's `renderpartofmodel` helper.
- Updated repository ignore rules so `HMG/eclipse/handmadeguns_Packs` content-pack sources are tracked while other HMG Eclipse runtime files remain ignored.

## Add gun skin usage tooltips

- Add a localized tooltip to every gun skin item explaining that skins are applied
  by crafting them with a compatible gun.
- Show universal unified-HMG-gun compatibility for valid skins and safely identify
  invalid skin data.
