# Siege Weapons

## Description

Siege Weapons is a Minecraft plugin that adds buildable medieval siege engines — trebuchets, catapults and battering rams — to a server where players hold territory worth besieging.

It is designed as a companion to [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions). Factions can already declare war, claim land and fight over it, but a wall is still only as strong as the pickaxe in front of it. Siege engines make breaching a stronghold a deliberate, visible, expensive act that the defenders can see coming.

This plugin was proposed from a Kingdom: First Era player request on 2026-09-04.

## Project Status

**In development, not yet released.** A trebuchet can be built, is remembered across restarts, can be loaded and fired with a cooldown between shots, and can be destroyed by breaking one of its blocks. It cannot yet be aimed, and its shots never break blocks. The scope of the first release is defined in [MVP.md](MVP.md) — it starts with a single engine, on purpose.

## Planned Features

- Multiblock siege engines built from an in-world pattern, not a crafting recipe
- Aim and fire, with a real projectile arc
- Block damage gated on Medieval Factions war state and claim ownership
- Configurable ammunition, range and cooldown

## Installation

Not yet released. Once there is a build, installation will follow the usual DPC pattern: drop the jar in `plugins/` and restart. It will also be available through [Dan's Plugin Manager](https://github.com/Dans-Plugins/Dans-Plugin-Manager).

### Required Companion Plugins

- [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) — supplies the war state and the claim ownership that gate block damage.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – How to build, load, fire and destroy a trebuchet
- [Configuration](CONFIG.md) – Every `config.yml` key
- [Changelog](CHANGELOG.md) – Changes since the last release
- [MVP](MVP.md) – Scope of the first release, and its deliberate non-goals

`COMMANDS.md` lands with the first release, per [DOCUMENTATION_PRACTICES.md](https://github.com/Dans-Plugins/dpc-conventions/blob/main/docs/DOCUMENTATION_PRACTICES.md).

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/Dans-Plugins/Siege-Weapons/issues/new).

## Contributing

This repository follows the standards in [dpc-conventions](https://github.com/Dans-Plugins/dpc-conventions). `CONTRIBUTING.md` has not been written yet.

### Building

Build with JDK 21 (the bytecode targets Java 17, so the jar also runs on the oldest supported server):

```bash
./gradlew clean build
```

This is the same command CI runs; it also runs the tests. The jar is written to `build/libs/Siege-Weapons-<version>.jar`. The Minecraft versions the plugin claims to support are listed in [minecraft-versions.json](minecraft-versions.json).

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).

See the [LICENSE](LICENSE) file for full details.
