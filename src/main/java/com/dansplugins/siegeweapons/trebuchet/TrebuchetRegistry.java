package com.dansplugins.siegeweapons.trebuchet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** The trebuchets currently built, keyed by the position of their anchor block. */
public final class TrebuchetRegistry {

    private record AnchorKey(String world, int x, int y, int z) {
    }

    private final Map<AnchorKey, Trebuchet> byAnchor = new LinkedHashMap<>();

    /**
     * Adds a trebuchet unless one is already anchored at the same position.
     *
     * @return {@code true} if the trebuchet was added
     */
    public boolean register(Trebuchet trebuchet) {
        Objects.requireNonNull(trebuchet, "trebuchet");
        return byAnchor.putIfAbsent(keyOf(trebuchet), trebuchet) == null;
    }

    public Optional<Trebuchet> findByAnchor(String world, int x, int y, int z) {
        return Optional.ofNullable(byAnchor.get(new AnchorKey(world, x, y, z)));
    }

    /** All registered trebuchets, in the order they were registered. */
    public Collection<Trebuchet> all() {
        return Collections.unmodifiableList(new ArrayList<>(byAnchor.values()));
    }

    /** Replaces the registry's contents, e.g. with trebuchets loaded from disk. */
    public void replaceAll(List<Trebuchet> trebuchets) {
        byAnchor.clear();
        trebuchets.forEach(this::register);
    }

    private static AnchorKey keyOf(Trebuchet trebuchet) {
        return new AnchorKey(trebuchet.world(), trebuchet.x(), trebuchet.y(), trebuchet.z());
    }
}
