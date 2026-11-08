# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- A trebuchet can be built from vanilla blocks. Placing the last block of the pattern registers the engine and tells the player which way it faces. See [USER_GUIDE.md](USER_GUIDE.md#building-a-trebuchet).
- Built trebuchets are saved to `plugins/SiegeWeapons/trebuchets.yml` and are reloaded when the server restarts.
- A trebuchet is destroyed when any block of its pattern is broken, burnt, blown up, moved by a piston, or changed by a player or mob, and is removed from `trebuchets.yml`. A player who breaks or changes the block is told the engine was destroyed. Right-clicking the load block of an engine that has lost a part any other way also destroys it. See [USER_GUIDE.md](USER_GUIDE.md#destroying-a-trebuchet).
- A trebuchet can be loaded with cobblestone and fired by right-clicking the centre of its axle. The shot flies in an arc along the engine's facing and explodes where it lands, hurting players and mobs. See [USER_GUIDE.md](USER_GUIDE.md#loading-and-firing).
- A trebuchet shot breaks blocks only in land claimed by a Medieval Factions faction at war with the firer's faction, judged block by block. Everywhere else it leaves terrain alone. A shot never sets off TNT. If Medieval Factions cannot answer, or has been disabled, the shot breaks nothing and a warning is logged. This cannot be configured. See [USER_GUIDE.md](USER_GUIDE.md#loading-and-firing).
- Medieval Factions is now a required dependency: Siege Weapons will not enable without a compatible, enabled copy of it.
- `config.yml`, with `trebuchet.explosion-power` (default `3.0`). See [CONFIG.md](CONFIG.md).
- A trebuchet has to wait between shots. The wait is set by `trebuchet.shot-cooldown-seconds` (default `10`, `0` turns it off). A trebuchet can be reloaded while it waits. See [USER_GUIDE.md](USER_GUIDE.md#loading-and-firing).
- A trebuchet can be aimed. Sneaking and right-clicking the centre of its axle lengthens its range by a step, wrapping back to the shortest range after the longest. The shot's launch speed is chosen so it lands that range away on level ground, and the firer is told the range on firing. The range is set by `trebuchet.min-range` (default `20`), `trebuchet.max-range` (default `80`) and `trebuchet.range-step` (default `10`). See [USER_GUIDE.md](USER_GUIDE.md#aiming).
