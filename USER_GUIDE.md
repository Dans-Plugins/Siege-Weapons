# Siege Weapons — User Guide

Siege Weapons is in development. So far a trebuchet can be built, is remembered across server restarts, can be aimed, loaded and fired with a cooldown between shots, and is destroyed when one of its blocks is lost. The rest of the first release is scoped in [MVP.md](MVP.md).

## Building a trebuchet

A trebuchet is built from vanilla blocks placed in the world. There is no item or recipe. When you place the last block of the arrangement below, the plugin recognises the engine, registers it and tells you which way it faces:

```
Trebuchet built, facing north.
```

### The pattern

The pattern is shown from above, for a trebuchet facing **north** (the top of each grid). Build it facing any of the four directions by rotating the whole arrangement.

Layer 1 (ground):

```
P P P
P P P
P P P
```

Layer 2:

```
. . .
F . F
. I .
```

Layer 3:

```
. . .
L L L
. . .
```

| Symbol | Block |
|---|---|
| `P` | Oak Planks |
| `F` | Oak Fence |
| `L` | Oak Log (any orientation) |
| `I` | Block of Iron |
| `.` | Anything: other blocks here do not stop detection |

The block of iron is the counterweight. It hangs **behind** the axle, so it decides the direction the trebuchet faces: the engine faces away from it.

The order you place the blocks in does not matter. Placing more blocks on an engine that has already been registered does nothing.

### Where trebuchets are stored

Built trebuchets are saved to `plugins/SiegeWeapons/trebuchets.yml`. Each one records its world, the position of the centre block of its ground layer, and its facing. An entry the plugin cannot read is skipped, and a warning is written to the server log.

## Loading and firing

The centre block of the axle (the middle `L` of layer 3) is the load block.

1. Hold **cobblestone** and right-click the load block. One cobblestone is taken from your hand and you are told `Trebuchet loaded.` A trebuchet holds one shot at a time.
2. Right-click the load block again, with anything in your hand, to fire. You are told the range it fired at, for example `Trebuchet fired at 20 blocks.`

Right-clicking an unloaded trebuchet without cobblestone in your hand tells you `Load the trebuchet with cobblestone first.`

After a shot, a trebuchet has to wait before it can fire again: 10 seconds by default, set by `trebuchet.shot-cooldown-seconds` (see [CONFIG.md](CONFIG.md)). You can reload it straight away, but trying to fire it before the wait is over tells you `The trebuchet can fire again in <seconds>s.` and it keeps its shot. Each trebuchet has its own cooldown.

The shot is thrown from just above the axle, in the direction the trebuchet faces, at 45 degrees. It flies in an arc and, on level ground, lands about the trebuchet's range away from the centre of its base (see [Aiming](#aiming)). Hills, walls and slopes in the way change where it actually comes down.

Where the shot lands it explodes, with the power set by `trebuchet.explosion-power` (see [CONFIG.md](CONFIG.md)). The explosion hurts players and mobs, but **never breaks blocks**. Block damage will only ever be allowed during a Medieval Factions war, against land claimed by the enemy, and that is not implemented yet.

A loaded trebuchet is unloaded when the server restarts, and its cobblestone is not returned. A restart also ends any cooldown.

## Aiming

A trebuchet always fires in the direction it faces, which is fixed when it is built. To turn it, rebuild it facing another way.

What you aim is its **range**: how far away, in blocks on level ground, its shot lands. A newly built trebuchet starts at the shortest range, 20 blocks by default.

**Sneak and right-click the load block** to lengthen the range by one step (10 blocks by default). You are told the new range:

```
Trebuchet range set to 30 blocks.
```

Lengthening it past the longest range (80 blocks by default) takes it back to the shortest. Sneaking and right-clicking never loads or fires the trebuchet, and you can aim it whether or not it is loaded. The shortest range, the longest range and the step are set in [CONFIG.md](CONFIG.md).

Each trebuchet keeps its own range. A server restart puts every trebuchet back at the shortest range, and so does destroying and rebuilding one.

## Destroying a trebuchet

Siege engines are meant to be raided. Every block of the pattern is a key block: when one is broken by a player, burnt, or blown up, the trebuchet is destroyed and removed from `trebuchets.yml`. A player who breaks the block is told:

```
Trebuchet destroyed.
```

The rest of the structure stays where it is. Putting the missing block back builds a new, unloaded trebuchet, exactly as if it had been placed for the first time. Breaking a block that is not part of the pattern, such as decoration in a `.` position, does nothing to the engine.
