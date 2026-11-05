package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

/**
 * Bukkit glue: hands each block broken, burnt, blown up, moved by a piston or changed by an entity
 * to the {@link TrebuchetDestroyer}.
 */
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        event.getBlocks().forEach(this::removed);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        event.getBlocks().forEach(this::removed);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        if (event.getTo() != block.getType() && !removed(block).isEmpty()
                && event.getEntity() instanceof Player player) {
            player.sendMessage("Trebuchet destroyed.");
        }
    }

    private List<Trebuchet> removed(Block block) {
        return destroyer.blockRemoved(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }
}
