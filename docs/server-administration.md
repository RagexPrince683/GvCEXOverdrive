# Handmade Guns Overdrive Server Administration

This guide focuses on running `HandmadeGuns` on a dedicated Forge 1.7.10 server.

## Deployment Checklist

- Install the same HMG jar on server and clients.
- Install the same content packs on server and clients.
- Start the server once to generate `config/HandmadeGuns.cfg`.
- Stop the server before editing configs.
- Restart after config or pack changes.

## Recommended Public-Server Settings

For safer terrain and predictable pickup behavior:

```properties
Gun.cfg_blockdestroy=false
ManualGunPickup.enableManualGunPickup=true
ManualGunPickup.manualGunPickupRequiresLineOfSight=true
ManualGunPickup.manualGunPickupOnlyGuns=true
ManualGunPickup.manualGunPickupRange=3.0
```

For lower visual/entity overhead:

```properties
Cartridge.cfg_canEjectCartridge=false
Render.enableVBOModelRendering=true
```

## Balancing Guidance

- Balance individual weapons in content-pack gun files first.
- Use `Gun.cfg_blockdestroy=false` for PvE/PvP servers where terrain griefing matters.
- Use manual pickup if dropped guns are high value and accidental pickup or item-vacuum behavior is undesirable.
- Keep content-pack recipe files consistent with your server economy.
- Review `cfg_KnockBack`, `cfg_KnockBackY`, and projectile definitions when tuning PvP.

## Content Pack Operations

HMG loads packs from `handmadeguns_Packs` and a legacy `mods/handmadeguns/addgun` path. Packs are sorted by folder/file name before loading. Keep pack names stable across server updates so item registration order is predictable.

When updating packs:

1. Back up the world and pack directory.
2. Stop the server.
3. Update the pack on server and clients.
4. Restart and watch the console for pack-load errors and warnings. If you need verbose content-pack confirmation or timing details, temporarily enable `Logging.enableDebugLogging` and then watch for `[Timing]` messages.
5. Test representative guns, magazines, bullets, recipes, sounds, and models before reopening the server.

## Commands for Admins

- `/reloadSettings` reloads gun files from `handmadeguns_Packs` and rebuilds client models on the side where it is run. It has permission level `0` in source, so restrict command access with external server tooling if needed.
- `/hmgmanual` reports Guide-API/HMG Field Manual status and also has permission level `0`.
