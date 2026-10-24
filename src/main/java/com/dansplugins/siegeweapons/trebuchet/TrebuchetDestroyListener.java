package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

/** Bukkit glue: hands each block broken, burnt or blown up to the {@link TrebuchetDestroyer}. */
public final class TrebuchetDestroyListener implements Listener {

    private final TrebuchetDestroyer destroyer;

    public TrebuchetDestroyListener(TrebuchetDestroyer destroyer) {
        this.destroyer = destroyer;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!removed(event.getBlock()).isEmpty()) {
            event.getPlayer().sendMessage("Trebuchet destroyed.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        removed(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach(this::removed);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().forEach(this::removed);
    }

    private List<Trebuchet> removed(Block block) {
        return destroyer.blockRemoved(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
}
