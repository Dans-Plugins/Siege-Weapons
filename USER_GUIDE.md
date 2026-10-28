# Siege Weapons — User Guide

Siege Weapons is in development. So far a trebuchet can be built, is remembered across server restarts, can be loaded and fired, and is destroyed when one of its blocks is lost. It cannot yet be aimed. The rest of the first release is scoped in [MVP.md](MVP.md).

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
2. Right-click the load block again, with anything in your hand, to fire. You are told `Trebuchet fired.`

Right-clicking an unloaded trebuchet without cobblestone in your hand tells you `Load the trebuchet with cobblestone first.`

The shot is thrown from just above the axle, in the direction the trebuchet faces, at 45 degrees. It flies in an arc and lands roughly 50 blocks away on level ground. Aiming is not implemented yet, so every shot from the same trebuchet lands in the same place.

Where the shot lands it explodes, with the power set by `trebuchet.explosion-power` (see [CONFIG.md](CONFIG.md)). The explosion hurts players and mobs, but **never breaks blocks**. Block damage will only ever be allowed during a Medieval Factions war, against land claimed by the enemy, and that is not implemented yet.

A loaded trebuchet is unloaded when the server restarts, and its cobblestone is not returned.

## Destroying a trebuchet

Siege engines are meant to be raided. Every block of the pattern is a key block: when one is broken by a player, burnt, or blown up, the trebuchet is destroyed and removed from `trebuchets.yml`. A player who breaks the block is told:

```
Trebuchet destroyed.
```

The rest of the structure stays where it is. Putting the missing block back builds a new, unloaded trebuchet, exactly as if it had been placed for the first time. Breaking a block that is not part of the pattern, such as decoration in a `.` position, does nothing to the engine.
