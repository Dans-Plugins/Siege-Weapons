# Siege Weapons — Configuration

Siege Weapons writes `plugins/SiegeWeapons/config.yml` the first time it starts. Edit it and restart the server to apply a change.

A value the plugin cannot use is replaced by its default, and a warning naming the key is written to the server log. A missing key also uses its default, without a warning.

## Keys

| Key | Type | Default | Meaning |
|---|---|---|---|
| `trebuchet.explosion-power` | number greater than 0 | `3.0` | Power of the explosion where a trebuchet shot lands. For comparison, TNT is `4`. |
| `trebuchet.shot-cooldown-seconds` | whole number from 0 to 86400 | `10` | Seconds a trebuchet must wait after firing before it can fire again. `0` turns the cooldown off. A trebuchet can be reloaded while it waits. |

## Block damage

There is no key for block damage, and there never will be. A trebuchet shot hurts players and mobs wherever it lands, but it never breaks blocks. Block damage will only ever be allowed when the shot lands in a chunk claimed by a Medieval Factions faction at war with the firer's faction, and that check is not implemented yet. See [MVP.md](MVP.md).
