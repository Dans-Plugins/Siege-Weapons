# Siege Weapons — User Guide

Siege Weapons is in development. So far a trebuchet can be built, is remembered across server restarts, and is destroyed when one of its blocks is lost. It cannot yet be loaded, aimed or fired. The rest of the first release is scoped in [MVP.md](MVP.md).

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

## Destroying a trebuchet

Siege engines are meant to be raided. Every block of the pattern is a key block: when one is broken by a player, burnt, or blown up, the trebuchet is destroyed and removed from `trebuchets.yml`. A player who breaks the block is told:

```
Trebuchet destroyed.
```

The rest of the structure stays where it is. Putting the missing block back builds a new trebuchet, exactly as if it had been placed for the first time. Breaking a block that is not part of the pattern, such as decoration in a `.` position, does nothing to the engine.
