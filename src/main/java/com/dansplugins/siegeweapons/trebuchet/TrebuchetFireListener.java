package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Bukkit glue: hands sneaking right-clicks on a block to {@link TrebuchetAiming} and other
 * right-clicks to {@link TrebuchetFiring}, launches the projectile a fired engine throws, and
 * explodes it where it lands.
 */
public final class TrebuchetFireListener implements Listener {

    /** What a trebuchet is loaded with. */
    public static final Material AMMUNITION = Material.COBBLESTONE;

    private final TrebuchetFiring firing;
    private final TrebuchetAiming aiming;
    private final TrebuchetSettings settings;
    private final Set<UUID> inFlight = new HashSet<>();

    public TrebuchetFireListener(TrebuchetFiring firing, TrebuchetAiming aiming, TrebuchetSettings settings) {
        this.firing = firing;
        this.aiming = aiming;
        this.settings = settings;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || block == null) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            aiming.adjust(block.getWorld().getName(), block.getX(), block.getY(), block.getZ())
                    .ifPresent(aim -> {
                        // Aiming must not also place the held block or open anything.
                        event.setCancelled(true);
                        player.sendMessage("Trebuchet range set to " + aim.range() + " blocks.");
                    });
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        firing.use(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), hand.getType() == AMMUNITION)
                .ifPresent(interaction -> {
                    // Using the load block must not also place the held block or open anything.
                    event.setCancelled(true);
                    switch (interaction.result()) {
                        case LOADED -> {
                            hand.setAmount(hand.getAmount() - 1);
                            player.sendMessage("Trebuchet loaded.");
                        }
                        case FIRED -> {
                            int range = aiming.rangeOf(interaction.trebuchet());
                            launch(block.getWorld(), interaction.trebuchet(), range, player);
                            player.sendMessage("Trebuchet fired at " + range + " blocks.");
                        }
                        case NOT_LOADED -> player.sendMessage("Load the trebuchet with cobblestone first.");
                        case COOLING_DOWN -> player.sendMessage("The trebuchet can fire again in "
                                + TrebuchetFiring.wholeSecondsUp(interaction.cooldownRemaining()) + "s.");
                    }
                });
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Entity projectile = event.getEntity();
        if (!inFlight.remove(projectile.getUniqueId())) {
            return;
        }
        Entity source = event.getEntity().getShooter() instanceof Entity shooter ? shooter : null;
        projectile.getWorld().createExplosion(projectile.getLocation(), settings.explosionPower(),
                false, settings.breaksBlocks(), source);
        projectile.remove();
    }

    private void launch(World world, Trebuchet trebuchet, int range, Player firer) {
        TrebuchetTrajectory.Launch launch = TrebuchetTrajectory.launchFrom(trebuchet, range);
        Snowball projectile = world.spawn(new Location(world, launch.x(), launch.y(), launch.z()), Snowball.class);
        projectile.setItem(new ItemStack(AMMUNITION));
        projectile.setShooter(firer);
        projectile.setVelocity(new Vector(launch.velocityX(), launch.velocityY(), launch.velocityZ()));
        inFlight.add(projectile.getUniqueId());
    }
}
