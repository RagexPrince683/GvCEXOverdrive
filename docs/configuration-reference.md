# Handmade Guns Overdrive Configuration Reference

The active config file is generated from the `HandmadeGuns` mod id, usually as `config/HandmadeGuns.cfg`. Defaults below are read directly from `HMG/src/main/java/handmadeguns/HandmadeGunsCore.java`.

## `Gun`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `MAXGUNSINV` | integer | `2` | Maximum gun inventory value used by HMG inventory/item systems. |
| `cfg_MuzzleFlash` | boolean | `true` | Enables muzzle flash behavior. |
| `cfg_ADS_Sneaking` | integer | `0` | Controls ADS/sneaking interaction mode. Exact modes are legacy and should be tested with your key setup. |
| `cfg_ADS_Key_Toggle` | boolean | `true` | Makes ADS key behavior toggle-style when enabled. |
| `cfg_Swap_Fire_And_ADS_Keys` | boolean | `false` | Swaps held-gun fire and ADS mouse behavior: fire uses attack/left-click, and `ADS_Key` defaults to use-item/right-click. |
| `cfg_Sneak_ByADSKey` | boolean | `false` | Allows the ADS key to trigger sneaking behavior. |
| `cfg_Avoid_ALL_ConflictKeys` | boolean | `true` | Enables HMG's conflict-avoidance handling for key input. |
| `cfg_blockdestroy` | boolean | `true` | Allows HMG explosive/projectile block destruction when the projectile also permits it. Disable for safer servers. |
| `cfg_AvoidHit` | string | empty | Avoid-hit entity class/string filter used by hit logic. |
| `cfg_ThreadHitCheck` | boolean | `true` | Enables threaded hit-check behavior. |
| `cfg_ThreadHitCheck_split_length` | integer | `10` | Segment length used by threaded hit checks. |
| `cfg_KnockBack` | double | `0.05` | Default horizontal knockback coefficient. |
| `cfg_KnockBackY` | double | `0.01` | Default vertical knockback coefficient. |

## `Render`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `cfg_ZoomRender` | boolean | `true` | Enables zoom rendering behavior. |
| `cfg_FOV` | integer | `95` | FOV value used by HMG render/handling systems. Source comments indicate some older render offsets were authored around 95 FOV. |
| `cfg_RenderPlayer` | boolean | `false` | Legacy render-player option; source comments indicate it may have no current usages. |
| `cfg_useStencil` | boolean | `false` | Controls stencil rendering path. |
| `enableVBOModelRendering` | boolean | `true` | Client-side: uses OpenGL VBOs for HMG OBJ model groups when possible. Disable to force legacy display-list rendering. |
| `cfg_Flash` | boolean | `true` | Enables flash render effects. |

## `ClientCamera`

Client-side only visual camera options for the UniMixins first-person Overdrive camera. These options do not change server movement, player rotation state, aiming, spread, or hit detection.

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `masterEnabled` | boolean | `true` | Master switch for all custom first-person camera transforms. |
| `rotationSmoothingEnabled` | boolean | `true` | Enables visual yaw/pitch smoothing from frame-to-frame view deltas without writing player rotation. |
| `smoothingStrength` | double | `0.18` | Lerp factor used by the visual rotation smoothing. |
| `maxYawOffset` | double | `3.0` | Clamp for visual yaw smoothing, in degrees. |
| `maxPitchOffset` | double | `3.0` | Clamp for visual pitch smoothing, in degrees. |
| `motionTiltEnabled` | boolean | `true` | Enables visual roll/pitch tilt from strafing, sprinting, jumping, falling, and landing. |
| `maxRoll` | double | `4.0` | Clamp for movement roll, in degrees. |
| `maxMovementPitch` | double | `3.0` | Clamp for movement pitch, in degrees. |
| `tiltReturnSpeed` | double | `0.15` | Return/lerp speed for movement tilt. |
| `customBobEnabled` | boolean | `true` | Enables smoother custom camera bob transforms. |
| `replaceVanillaBob` | boolean | `false` | When true, cancels vanilla view bobbing and applies only HMG custom bob; when false, adds custom bob after vanilla bob. |
| `bobStrength` | double | `0.45` | Custom bob amount. |
| `bobSpeed` | double | `9.0` | Custom bob phase speed. |
| `sprintBobMultiplier` | double | `1.25` | Bob speed multiplier while sprinting. |
| `adsBobMultiplier` | double | `0.35` | Bob strength multiplier while ADS is active. |
| `fovInertiaEnabled` | boolean | `true` | Smooths client-only FOV transitions without writing `gameSettings.fovSetting`. |
| `fovLerpSpeed` | double | `0.12` | General FOV inertia lerp factor. |
| `sprintFovBoost` | double | `3.0` | Additive visual sprint FOV boost. |
| `adsFovSpeed` | double | `0.22` | FOV lerp factor while ADS is active. |
| `shakeEnabled` | boolean | `true` | Enables additive stacked camera shake sources. |
| `maxShakePitch` | double | `5.0` | Clamp for shake pitch, in degrees. |
| `maxShakeYaw` | double | `3.0` | Clamp for shake yaw, in degrees. |
| `maxShakeRoll` | double | `4.0` | Clamp for shake roll, in degrees. |
| `shakeDecaySpeed` | double | `0.18` | Decay/return speed for shake sources. |
| `recoilShakeMultiplier` | double | `0.08` | Multiplier for visual recoil shake impulses. |
| `explosionShakeMultiplier` | double | `0.45` | Multiplier for explosion shake impulses. |
| `landingShakeMultiplier` | double | `0.8` | Multiplier for hard-landing shake impulses. |
| `damageShakeMultiplier` | double | `0.7` | Multiplier for damage shake impulses. |
| `customHurtCameraEnabled` | boolean | `true` | Enables HMG custom hurt-camera supplement/replacement behavior. |
| `replaceVanillaHurtCamera` | boolean | `false` | When true, cancels vanilla hurt camera effect and applies the HMG visual shake path; when false, supplements vanilla. |
| `debugCamera` | boolean | `false` | Reserved debug switch for camera diagnostics. |

## `Cartridge`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `cfg_canEjectCartridge` | boolean | `true` | Enables ejected cartridge entities/effects. |
| `cfg_Cartridgetime` | integer | `200` | Cartridge lifetime/fuse value in ticks. |

## `ManualGunPickup`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `enableManualGunPickup` | boolean | `false` | When true, dropped HMG gun items require the `Pickup HMG Gun` key instead of normal walk-over pickup. |
| `manualGunPickupRange` | double | `3.0` | Maximum pickup request distance in blocks. Source clamps config UI range from `0.1` to `8.0`. |
| `manualGunPickupRequiresLineOfSight` | boolean | `true` | Server requires the player to look at the dropped gun with no block in the way. |
| `manualGunPickupOnlyGuns` | boolean | `true` | Restricts manual pickup to HMG gun items. If false, other HandmadeGuns items may also use it; non-HMG items are not affected. |
| `enableGunGroundPhysicsRender` | boolean | `false` | Client-side: renders supported dropped HMG guns with a flatter physical-looking ground orientation. |

## `GuideBook`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `enableHMGGuideBook` | boolean | `true` | Enables optional Guide-API HMG Field Manual registration. HMG still loads without Guide-API. |

## `LMM`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `cfg_FriendFireLMM` | boolean | `true` | LittleMaidMobX-related friendly-fire handling. |
| `cfg_FriendFirePlayerToLMM` | boolean | `true` | Allows player-to-LMM friendly-fire behavior where LMM compatibility code applies. |
| `cfg_RenderGunSizeLMM` | boolean | `false` | LMM gun-size render compatibility option. |
| `cfg_RenderGunAttachmentLMM` | boolean | `false` | LMM attachment-render compatibility option. |

## World Generation Notes

HMG registers a world generator for the overworld. It generates copper and aluminum ores only when external ore-dictionary entries are not detected:

- Copper: 12 veins/chunk, vein size 9, Y 20-63.
- Aluminum: 9 veins/chunk, vein size 7, Y 32-95.

These ore-generation toggles are computed automatically from ore dictionary checks; no active config keys for ore generation were found in the inspected HMG code.
