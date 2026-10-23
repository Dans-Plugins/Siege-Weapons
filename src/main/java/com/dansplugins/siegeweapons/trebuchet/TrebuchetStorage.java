package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/** Saves and loads built trebuchets as YAML, so they survive a server restart. */
public final class TrebuchetStorage {

    private static final String ROOT = "trebuchets";

    private final File file;
    private final Consumer<String> warn;

    /**
     * @param file the YAML file to read and write
     * @param warn receives a message for each stored entry that cannot be read and is skipped
     */
    public TrebuchetStorage(File file, Consumer<String> warn) {
        this.file = file;
        this.warn = warn;
    }

    public void save(Collection<Trebuchet> trebuchets) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Trebuchet trebuchet : trebuchets) {
            String path = ROOT + "." + trebuchet.id();
            yaml.set(path + ".world", trebuchet.world());
            yaml.set(path + ".x", trebuchet.x());
            yaml.set(path + ".y", trebuchet.y());
            yaml.set(path + ".z", trebuchet.z());
            yaml.set(path + ".facing", trebuchet.facing().name());
        }
        yaml.save(file);
    }

    /** Loads stored trebuchets; a missing file means none have been built yet. */
    public List<Trebuchet> load() {
        List<Trebuchet> trebuchets = new ArrayList<>();
        if (!file.exists()) {
            return trebuchets;
        }
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file).getConfigurationSection(ROOT);
        if (root == null) {
            return trebuchets;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection entry = root.getConfigurationSection(key);
            try {
                trebuchets.add(read(key, entry));
            } catch (IllegalArgumentException | NullPointerException e) {
                warn.accept("Skipping unreadable trebuchet '" + key + "' in " + file.getName() + ": " + e.getMessage());
            }
        }
        return trebuchets;
    }

    private static Trebuchet read(String key, ConfigurationSection entry) {
        if (entry == null) {
            throw new IllegalArgumentException("not a section");
        }
        for (String field : List.of("x", "y", "z")) {
            if (!entry.isInt(field)) {
                throw new IllegalArgumentException("missing or non-integer '" + field + "'");
            }
        }
        return new Trebuchet(
                UUID.fromString(key),
                entry.getString("world"),
                entry.getInt("x"),
                entry.getInt("y"),
                entry.getInt("z"),
                Facing.valueOf(String.valueOf(entry.getString("facing"))));
    }
}
