# Siege Weapons — User Guide

Siege Weapons is in development. So far a trebuchet can be built and is remembered across server restarts. It cannot yet be loaded, aimed or fired, and it cannot be destroyed as a unit. The rest of the first release is scoped in [MVP.md](MVP.md).

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
