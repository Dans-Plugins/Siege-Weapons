package com.dansplugins.siegeweapons.trebuchet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    // Set when the file exists but could not be read as a whole (a YAML error, or content that
    // is not this plugin's). Until a later load succeeds, save() refuses to run: the registry
    // is then empty, and writing it would replace the very file that could not be read.
    private boolean loadFailed;

    /**
     * @param file the YAML file to read and write
     * @param warn receives a message for each stored entry that cannot be read and is skipped
     */
    public TrebuchetStorage(File file, Consumer<String> warn) {
        this.file = file;
        this.warn = warn;
    }

    /**
     * Replaces the file with {@code trebuchets}, through a sibling temp file moved into place so a
     * crash mid-save leaves the previous file whole.
     *
     * @throws IOException if the file could not be written, or if the last {@link #load} could not
     *         read it, in which case it is left exactly as it was
     */
    public void save(Collection<Trebuchet> trebuchets) throws IOException {
        if (loadFailed) {
            throw new IOException(file.getName() + " could not be read when the plugin was enabled, so it was not "
                    + "saved over; fix or restore it and restart the server");
        }
        YamlConfiguration yaml = new YamlConfiguration();
        for (Trebuchet trebuchet : trebuchets) {
            String path = ROOT + "." + trebuchet.id();
            yaml.set(path + ".world", trebuchet.world());
            yaml.set(path + ".x", trebuchet.x());
            yaml.set(path + ".y", trebuchet.y());
            yaml.set(path + ".z", trebuchet.z());
            yaml.set(path + ".facing", trebuchet.facing().name());
        }
        Path target = file.toPath();
        Path temp = target.resolveSibling(file.getName() + ".tmp");
        yaml.save(temp.toFile());
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Whether the last {@link #load} could not read the file, in which case {@link #save} refuses. */
    public boolean isLoadFailed() {
        return loadFailed;
    }

    /**
     * Loads stored trebuchets; a missing file means none have been built yet. A file that cannot be
     * parsed, or that holds content but no {@code trebuchets} section, loads nothing and makes
     * every later {@link #save} refuse until a load succeeds (see {@link #isLoadFailed}). A single
     * unreadable entry is skipped with a warning and the rest load.
     */
    public List<Trebuchet> load() {
        loadFailed = false;
        List<Trebuchet> trebuchets = new ArrayList<>();
        if (!file.exists()) {
            return trebuchets;
        }
        // Not YamlConfiguration.loadConfiguration: it reports a parse failure only by returning an
        // empty configuration, which looks the same as a file with no trebuchets in it.
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            loadFailed = true;
            warn.accept("Could not read " + file.getName() + ", so no trebuchets were loaded and the file will not be "
                    + "saved over: " + e.getMessage());
            return trebuchets;
        }
        ConfigurationSection root = yaml.getConfigurationSection(ROOT);
        if (root == null) {
            // save() writes nothing but the section, so any other content is a hand edit worth keeping.
            if (!yaml.getKeys(false).isEmpty()) {
                loadFailed = true;
                warn.accept("Found no '" + ROOT + "' section in " + file.getName() + ", so no trebuchets were loaded "
                        + "and the file will not be saved over.");
            }
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
