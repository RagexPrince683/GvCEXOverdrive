# Handmade Guns Content Pack Guide

HMG is pack-driven. Content packs can add guns, magazines, bullets, attachments, recipes, sounds, textures, models, tabs, and scripts without changing Java code.

## Supported Pack Roots

Preferred path:

```text
handmadeguns_Packs/<PackName>/
```

Legacy path still read by source:

```text
mods/handmadeguns/addgun/<PackName>/
```

## Common Pack Layout

```text
handmadeguns_Packs/
  ExamplePack/
    guns/
    magazines/
    bullets/
    attachment/
    addpackrecipe/
    addTab/
    addmodel/
    addtexture/
    addsighttex/
    addsounds/
    addscripts/
    scripts/
    additionalSettings.txt
```

## Loader Behavior

- Pack folders are sorted by name before loading.
- Files inside major definition folders are sorted by name where the source explicitly sorts them.
- Resource folders are copied into generated `assets/handmadeguns` paths under the pack root and registered as resource containers on the client.
- Client resource reload is triggered after pack resources are scanned.
- `additionalSettings.txt` is read with Shift-JIS encoding.

## Definition Folders

| Folder/File | Purpose |
| --- | --- |
| `guns/` | Gun definition files parsed by `HMGGunMaker`. |
| `magazines/` | Magazine definitions parsed by `HMGAddmagazine`. |
| `bullets/` | Bullet/projectile definitions parsed by `HMGAddBullets`. |
| `attachment/` | Attachment definitions parsed by `HMGAddAttachment`. |
| `addpackrecipe/` | Recipe files loaded into both the original crafting system and Gun Smithing GUI registry. |
| `addTab/` | Creative-tab definitions. |
| `addmodel/` | Model resources copied to `textures/model`. |
| `addtexture/` | Item texture resources copied to `textures/items`. |
| `addsighttex/` | Sight/overlay textures copied to `textures/misc`. |
| `addsounds/` | Sound files copied to `sounds` and processed by the HMG sound loader. |
| `addscripts/` | JavaScript files copied to the legacy HMG scripts resource path and evaluated during pre-init. |
| `scripts/` | JavaScript files evaluated during pre-init. |
| `additionalSettings.txt` | Optional pack-level multipliers such as `damageCof` and `speedCof`. |

## `additionalSettings.txt`

Recognized keys found in source:

```text
damageCof,1.0
speedCof,1.0
```

The file is read per pack. The current source switch lacks `break` statements between these two cases, so pack authors should test carefully when setting either multiplier.

## Pack Author Tips

- Keep file names deterministic and stable.
- Clearly document which magazines and bullets each gun accepts.
- Ship client assets with the pack and test on a clean client.
- Test `/reloadSettings` during development, but do full restarts before release validation.
- Avoid relying on undocumented parser behavior; HMG definition parsers are legacy and forgiving in some places but not uniformly validated.
