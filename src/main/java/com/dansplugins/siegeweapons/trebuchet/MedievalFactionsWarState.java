package com.dansplugins.siegeweapons.trebuchet;

import com.dansplugins.factionsystem.MedievalFactions;
import com.dansplugins.factionsystem.claim.MfClaimedChunk;
import com.dansplugins.factionsystem.faction.MfFaction;
import com.dansplugins.factionsystem.faction.MfFactionId;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.UUID;

/**
 * Bukkit glue: answers the {@link WarGate}'s questions from the running Medieval Factions. A
 * faction is identified by its Medieval Factions id, so renaming one changes nothing.
 *
 * <p>Medieval Factions comes from the installed plugin and is never bundled.
 */
public final class MedievalFactionsWarState implements WarGate.Factions {

    /** The name Medieval Factions registers under, as {@code depend} names it in plugin.yml. */
    public static final String PLUGIN_NAME = "MedievalFactions";

    private final MedievalFactions medievalFactions;

    private MedievalFactionsWarState(MedievalFactions medievalFactions) {
        this.medievalFactions = medievalFactions;
    }

    /**
     * The war state of {@code plugin}.
     *
     * @return empty if {@code plugin} is missing, not enabled, or a build of Medieval Factions
     *         whose classes do not match the ones this plugin was built against
     */
    public static Optional<WarGate.Factions> of(Plugin plugin) {
        if (plugin == null || !plugin.isEnabled()) {
            return Optional.empty();
        }
        try {
            return plugin instanceof MedievalFactions medievalFactions
                    ? Optional.of(new MedievalFactionsWarState(medievalFactions))
                    : Optional.empty();
        } catch (NoClassDefFoundError e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> factionOf(UUID player) {
        MfFaction faction = medievalFactions.getServices().getFactionService().getFactionByPlayerId(player.toString());
        return faction == null ? Optional.empty() : Optional.of(faction.getId());
    }

    @Override
    public Optional<String> claimOwner(String world, int chunkX, int chunkZ) {
        World bukkitWorld = medievalFactions.getServer().getWorld(world);
        if (bukkitWorld == null) {
            return Optional.empty();
        }
        MfClaimedChunk claim = medievalFactions.getServices().getClaimService()
                .getClaim(bukkitWorld.getUID(), chunkX, chunkZ);
        return claim == null ? Optional.empty() : Optional.of(claim.getFactionId());
    }

    @Override
    public boolean atWar(String faction, String otherFaction) {
        return medievalFactions.getServices().getFactionRelationshipService()
                .getFactionsAtWarWithByFactionId(faction).stream()
                .map(MfFactionId::getValue)
                .anyMatch(otherFaction::equals);
    }
}
