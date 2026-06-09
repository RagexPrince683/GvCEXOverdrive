# Getting Started with Handmade Guns Overdrive

This guide introduces the actively maintained `HMG/` module: Handmade Guns Overdrive.

## 1. Install HMG

Install on both client and server:

1. Minecraft 1.7.10.
2. Forge 10.13.4.1614.
3. Handmade Guns Overdrive (`HandmadeGuns`).
4. Any content packs required by your world or server.
5. Optional: Guide-API if you want the HMG Field Manual item.

Start the game once to generate `config/HandmadeGuns.cfg`.

## 2. Review Controls

HMG registers key bindings under the `HandmadeGuns` category. Defaults found in source include:

| Action | Default |
| --- | --- |
| Reload Magazine | `R` |
| Fire AttachedGun | `F` |
| ADS_Key | mouse button `-100` / left-click style binding in the legacy key system |
| Gun Prepare Modification Key | Left Alt |
| Attachment GUI | `X` |
| Change Magazine Type | `B` |
| Fix Gun | `H` |
| Gun Settings Modification | unbound |
| Zero in: Increase | `Y` |
| Zero in: Reset | `H` |
| Zero in: Decrease | `N` |
| Seeker Open/Close | `C` |
| Cycle Selector | `F` |
| Pickup HMG Gun | `P`, only used when manual pickup is enabled |

Because several defaults overlap, review and rebind keys before playing seriously.

## 3. Learn the Core Items

HMG adds:

- **Gun Smithing Table** for the GUI crafting system.
- **Crafting materials** such as Polymer, Steel Ingot, Copper Ingot, Aluminum Ingot, and Iron-Carbon Alloy.
- **Gun parts** such as firing pins, trigger assemblies, receivers, bolt assemblies, barrel kits, AR parts, feed modules, launcher components, stock/optic mounts, and wood furniture.
- **Gun racks / item holders** for displaying or storing guns.
- **Pack-defined guns, magazines, bullets, and attachments** loaded from content packs.

## 4. First Survival Workflow

1. Mine or acquire metals and standard vanilla materials.
2. Craft Polymer from slime ball + reeds.
3. Use HMG metal and part recipes to make receiver/barrel/assembly components.
4. Craft or obtain a Gun Smithing Table.
5. Use the table or pack recipes to craft compatible guns, magazines, ammunition, and attachments.
6. Match ammunition to the gun's content-pack definition.

## 5. Optional Manual Pickup

If `ManualGunPickup.enableManualGunPickup=true`, dropped HMG guns are not picked up just by walking over them. Look at the dropped gun and press `Pickup HMG Gun`, default `P`. Servers can require line of sight and can set the pickup range.

## 6. Use the Field Manual When Available

If Guide-API is installed and `GuideBook.enableHMGGuideBook=true`, HMG registers an in-game Field Manual. Run `/hmgmanual` to confirm whether the manual is enabled, missing Guide-API, registered, failed, or pending.
