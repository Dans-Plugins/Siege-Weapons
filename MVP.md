# Siege Weapons — MVP

## Problem

Medieval Factions gives players nations, borders and wars. It does not give them a way to *take* a fortification that feels like anything other than mining. Players asked for siege weapons because the interesting part of a medieval war is the siege, and right now the siege is a diamond pickaxe.

## Target environment

Spigot, on the Minecraft versions listed in [minecraft-versions.json](minecraft-versions.json) (currently 1.19.4, 1.21.11, 26.2 and 26.3), **vanilla clients**. There is no way to give a client a custom entity model, so a siege engine has to be built out of blocks the player can already see. That is a constraint but also the right answer — a trebuchet made of real blocks is something you can walk up to, defend, and burn down.

## The smallest shippable slice

### v1 does

1. **One engine: the trebuchet.** Built as a multiblock — a specific arrangement of blocks the player places in the world. Detected on completion, registered, and persisted.
2. **Load and fire.** A player loads ammunition into a designated block of the structure and fires it. The projectile follows a real arc and explodes on impact, with configurable power.
3. **Aim.** Facing and range are set from the structure and a simple command or interaction, so aiming is a deliberate act rather than a crosshair.
4. **Block damage gated on war.** A shot only breaks blocks when the target chunk is claimed by a faction the firing player's faction is at war with. Everywhere else the projectile still explodes and still hurts players and mobs, but leaves terrain alone. This is the single most important rule in the plugin.
5. **Destructible.** Breaking a key block of the structure destroys the engine. Siege engines are meant to be raided.
6. **A cooldown between shots**, configurable.

### v1 does not

- **Ship catapults, ballistae, rams or towers.** One engine, proven, before a family of them.
- **Provide custom models or textures.** The engine is the blocks it is built from.
- **Support crewed operation** — no loader/aimer/firer roles, no multi-player requirement.
- **Move.** A v1 trebuchet is emplaced and stays where it was built. A battering ram implies a moving multiblock, which is a different and much larger problem.
- **Do naval or mounted siege.**
- **Damage blocks outside a declared war**, ever, under any config option. Making that configurable turns the plugin into a griefing tool on any server that installs it carelessly.
- **Integrate with an economy.** Cost is the build materials.

## Why these non-goals

Every siege plugin that has ever been abandoned was abandoned in the middle of the second engine type. The valuable, novel part here is not the physics — it is the *rule* that a trebuchet is only a siege weapon during a war against a faction that owns the ground. That rule is what makes the plugin safe to install, and it is worth getting exactly right on one engine before it has to hold for six.

## Open questions

- Does the trebuchet damage blocks by explosion, or by removing them individually? An explosion is simple and drops items; individual removal gives precise control over what a shot can and cannot breach (bedrock, claim cores, chests).
- Should there be a defender-facing warning — a Herald-style announcement when an engine is completed inside or near contested land? Probably yes; a siege nobody notices is not a siege.
- What happens to an engine when the war ends? Left standing, or disarmed?
- Should ammunition be a Conquest-Recipes item, so building the engine and stocking it are both medieval-flavoured?
- Medieval Factions is Kotlin; this plugin should match rather than mixing build tooling across the pair.

## Dependencies

- [Medieval Factions](https://github.com/Dans-Plugins/Medieval-Factions) — **hard dependency.** Without a war state and claim ownership, v1 has no rule to gate block damage on, and should refuse to enable rather than fall back to unrestricted damage.
