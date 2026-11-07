package com.dansplugins.siegeweapons;

import com.dansplugins.siegeweapons.trebuchet.MedievalFactionsWarState;
import com.dansplugins.siegeweapons.trebuchet.ShotBlast;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetAiming;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetBuildListener;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetBuilder;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetDestroyListener;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetDestroyer;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetFireListener;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetFiring;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetRegistry;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetSettings;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetStorage;
import com.dansplugins.siegeweapons.trebuchet.WarGate;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Entry point for SiegeWeapons. Features are scoped in MVP.md; so far a trebuchet can be built,
 * is remembered across restarts, can be aimed, loaded and fired with a cooldown between shots,
 * and is destroyed when one of its blocks is lost. Its shots break blocks only in land claimed by
 * a faction at war with the firer's, so the plugin refuses to run without Medieval Factions.
 */
public final class SiegeWeapons extends JavaPlugin {

    private final TrebuchetRegistry trebuchets = new TrebuchetRegistry();
    private TrebuchetStorage storage;

    @Override
    public void onEnable() {
        Optional<WarGate.Factions> factions = MedievalFactionsWarState.of(
                getServer().getPluginManager().getPlugin(MedievalFactionsWarState.PLUGIN_NAME));
        if (factions.isEmpty()) {
            // Never fall back to unrestricted damage (MVP.md): no war state, no plugin.
            getLogger().severe("A compatible Medieval Factions is not enabled, so Siege Weapons is disabling itself.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create the plugin data folder; built trebuchets will not be saved.");
        }
        saveDefaultConfig();
        TrebuchetSettings settings = TrebuchetSettings.from(getConfig(), getLogger()::warning);
        storage = new TrebuchetStorage(new File(getDataFolder(), "trebuchets.yml"), getLogger()::warning);
        trebuchets.replaceAll(storage.load());
        getServer().getPluginManager().registerEvents(
                new TrebuchetBuildListener(new TrebuchetBuilder(trebuchets, storage, getLogger()::warning)), this);
        TrebuchetDestroyer destroyer = new TrebuchetDestroyer(trebuchets, storage, getLogger()::warning);
        getServer().getPluginManager().registerEvents(new TrebuchetDestroyListener(destroyer), this);
        getServer().getPluginManager().registerEvents(
                new TrebuchetFireListener(destroyer,
                        new TrebuchetFiring(trebuchets, settings.shotCooldown(), System::nanoTime),
                        new TrebuchetAiming(trebuchets, settings.minRange(), settings.maxRange(), settings.rangeStep()),
                        settings, new ShotBlast(new WarGate(factions.get(), getLogger()::warning))), this);
    }

    @Override
    public void onDisable() {
        if (storage == null) {
            return;
        }
        try {
            storage.save(trebuchets.all());
        } catch (IOException e) {
            getLogger().warning("Could not save trebuchets: " + e.getMessage());
        }
    }
}
