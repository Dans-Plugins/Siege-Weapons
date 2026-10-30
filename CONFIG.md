# Siege Weapons — Configuration

Siege Weapons writes `plugins/SiegeWeapons/config.yml` the first time it starts. Edit it and restart the server to apply a change.

A value the plugin cannot use is replaced by its default, and a warning naming the key is written to the server log. A missing key also uses its default, without a warning.

## Keys

| Key | Type | Default | Meaning |
|---|---|---|---|
| `trebuchet.explosion-power` | number greater than 0 | `3.0` | Power of the explosion where a trebuchet shot lands. For comparison, TNT is `4`. |
| `trebuchet.shot-cooldown-seconds` | whole number from 0 to 86400 | `10` | Seconds a trebuchet must wait after firing before it can fire again. `0` turns the cooldown off. A trebuchet can be reloaded while it waits. |
| `trebuchet.min-range` | whole number from 1 to 150 | `20` | Shortest range of a trebuchet shot, in blocks on level ground. A newly built trebuchet starts at this range. |
| `trebuchet.max-range` | whole number from 1 to 150 | `80` | Longest range of a trebuchet shot, in blocks on level ground. Aiming past it goes back to `trebuchet.min-range`. |
| `trebuchet.range-step` | whole number from 1 to 150 | `10` | How many blocks each aiming adjustment adds to the range. The last step before `trebuchet.max-range` is shortened to land on it exactly. |

If `trebuchet.min-range` is greater than `trebuchet.max-range`, both use their defaults and a warning is written to the server log. Set them equal to give every trebuchet a single, fixed range. A range of only a few blocks lands the shot on or beside the trebuchet itself, and its explosion can hurt whoever fired it. See [USER_GUIDE.md](USER_GUIDE.md#aiming) for how players aim.

## Block damage

There is no key for block damage, and there never will be. A trebuchet shot hurts players and mobs wherever it lands, but it never breaks blocks. Block damage will only ever be allowed when the shot lands in a chunk claimed by a Medieval Factions faction at war with the firer's faction, and that check is not implemented yet. See [MVP.md](MVP.md).
