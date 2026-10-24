package com.dansplugins.siegeweapons;

import com.dansplugins.siegeweapons.trebuchet.TrebuchetBuildListener;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetBuilder;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetDestroyListener;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetDestroyer;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetRegistry;
import com.dansplugins.siegeweapons.trebuchet.TrebuchetStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * Entry point for SiegeWeapons. Features are scoped in MVP.md; so far a trebuchet can be built,
 * is remembered across restarts and is destroyed when one of its blocks is lost, but it cannot
 * yet be loaded, aimed or fired.
 */
public final class SiegeWeapons extends JavaPlugin {

    private final TrebuchetRegistry trebuchets = new TrebuchetRegistry();
    private TrebuchetStorage storage;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create the plugin data folder; built trebuchets will not be saved.");
        }
        storage = new TrebuchetStorage(new File(getDataFolder(), "trebuchets.yml"), getLogger()::warning);
        trebuchets.replaceAll(storage.load());
        getServer().getPluginManager().registerEvents(
                new TrebuchetBuildListener(new TrebuchetBuilder(trebuchets, storage, getLogger()::warning)), this);
        getServer().getPluginManager().registerEvents(
                new TrebuchetDestroyListener(new TrebuchetDestroyer(trebuchets, storage, getLogger()::warning)), this);
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
