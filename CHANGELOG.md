# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

### Added

- A trebuchet can be built from vanilla blocks. Placing the last block of the pattern registers the engine and tells the player which way it faces. See [USER_GUIDE.md](USER_GUIDE.md#building-a-trebuchet).
- Built trebuchets are saved to `plugins/SiegeWeapons/trebuchets.yml` and are reloaded when the server restarts.
- A trebuchet is destroyed when any block of its pattern is broken, burnt or blown up, and is removed from `trebuchets.yml`. A player who breaks the block is told the engine was destroyed. See [USER_GUIDE.md](USER_GUIDE.md#destroying-a-trebuchet).
- A trebuchet can be loaded with cobblestone and fired by right-clicking the centre of its axle. The shot flies in an arc along the engine's facing and explodes where it lands, hurting players and mobs but never breaking blocks. See [USER_GUIDE.md](USER_GUIDE.md#loading-and-firing).
- `config.yml`, with `trebuchet.explosion-power` (default `3.0`). See [CONFIG.md](CONFIG.md).
