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

## `Compatibility`

| Key | Type | Default | Effect |
| --- | --- | --- | --- |
| `enableCombativesRecoilIntegration` | boolean | `true` | Client-side: when Combatives is installed and its camera API is active, first-person HMG weapon recoil is submitted to Combatives as visual camera impulses. When Combatives is absent, disabled, unavailable, or rejects the base shot impulse, HMG uses its legacy look-rotation recoil fallback. |
| `enableCombativesAimRecoilIntegration` | boolean | `true` | Client-side: when a Combatives visual shot is accepted, HMG also applies actual local pitch/yaw aim recoil with delayed contribution-based recovery. |
| `combativesAimRecoilVerticalScale` | double | `0.55` | Scales real vertical aim displacement derived from HMG weapon recoil stats. |
| `combativesAimRecoilHorizontalScale` | double | `0.45` | Scales real horizontal aim displacement derived from the deterministic burst drift. |
| `combativesAimRecoilRecoveryDelayMs` | integer | `120` | Delay after the most recent accepted shot before controller-owned aim recoil begins recovering. |
| `combativesAimRecoilRecoverySpeed` | double | `3.0` | Recovery speed for controller-owned aim recoil; higher values recover faster. |
| `combativesAimRecoilMaxPitch` | double | `14.0` | Maximum controller-owned vertical aim recoil, in degrees. |
| `combativesAimRecoilMaxYaw` | double | `5.0` | Maximum controller-owned horizontal aim recoil, in degrees. |
| `enableCombativesRecoilDebug` | boolean | `false` | Client-side: enables verbose HMG-to-Combatives recoil diagnostics, including submitted visual impulse channels, timings, stacking mode, acceptance, fallback, weapon-state reset logs, aim-recoil pending/applied/recovered state, detected mouse deltas, and ownership. Leave disabled during normal gameplay. |

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
