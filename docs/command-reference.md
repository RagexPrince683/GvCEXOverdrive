# Handmade Guns Command Reference

HMG registers two commands from the current source.

## `/reloadSettings`

| Property | Value |
| --- | --- |
| Class | `handmadeguns.command.HMG_CommandReloadparm` |
| Permission level | `0` |
| Usage string | Source returns `null` |
| Registration | Server command; also registered with Forge client command handler |

Reloads gun definition files from `handmadeguns_Packs`, then calls the HMG proxy model setup. It is useful during pack development, but a full restart is still recommended for release/server validation because not every resource, recipe, registry, or server/client state is guaranteed to behave like a clean startup.

## `/hmgmanual`

| Property | Value |
| --- | --- |
| Class | `handmadeguns.command.HMG_CommandManual` |
| Permission level | `0` |
| Usage string | `/hmgmanual` |
| Registration | Server command |

Reports the optional HMG Field Manual state:

- Disabled by `GuideBook.enableHMGGuideBook=false`.
- Guide-API missing.
- Registered successfully.
- Registration failed.
- Registration pending.

## Permissions

Both commands return permission level `0` in source. On public servers, use your server wrapper, permissions plugin, or command-filtering tooling if you do not want all players to run them.
