package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Locale;

/** Bukkit glue: hands each placed block to the {@link TrebuchetBuilder}. */
public final class TrebuchetBuildListener implements Listener {

    private final TrebuchetBuilder builder;

    public TrebuchetBuildListener(TrebuchetBuilder builder) {
        this.builder = builder;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        World world = placed.getWorld();
        for (Trebuchet trebuchet : builder.blockPlaced(world.getName(),
                (x, y, z) -> world.getBlockAt(x, y, z).getType(),
                placed.getX(), placed.getY(), placed.getZ())) {
            event.getPlayer().sendMessage(
                    "Trebuchet built, facing " + trebuchet.facing().name().toLowerCase(Locale.ROOT) + ".");
        }
    }
}
