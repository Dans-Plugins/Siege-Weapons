package com.dansplugins.siegeweapons.trebuchet;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarGateTest {

    private static final String WORLD = "world";
    private static final String ATTACKERS = "attackers";
    private static final String ENEMIES = "enemies";
    private static final String NEUTRALS = "neutrals";

    private static final class FakeFactions implements WarGate.Factions {
        private final Map<UUID, String> members = new HashMap<>();
        private final Map<String, String> claims = new HashMap<>();
        private final Set<String> wars = new HashSet<>();

        void claim(String world, int chunkX, int chunkZ, String faction) {
            claims.put(world + ":" + chunkX + ":" + chunkZ, faction);
        }

        @Override
        public Optional<String> factionOf(UUID player) {
            return Optional.ofNullable(members.get(player));
        }

        @Override
        public Optional<String> claimOwner(String world, int chunkX, int chunkZ) {
            return Optional.ofNullable(claims.get(world + ":" + chunkX + ":" + chunkZ));
        }

        @Override
        public boolean atWar(String faction, String otherFaction) {
            return wars.contains(faction + ":" + otherFaction);
        }
    }

    private final UUID firer = UUID.randomUUID();
    private final FakeFactions factions = new FakeFactions();
    private final List<String> warnings = new ArrayList<>();
    private final WarGate gate = new WarGate(factions, warnings::add);

    /** The firer is in a faction at war with the owner of chunk (0, 0), the case every other test varies. */
    private void firerIsAtWarWithTheOwnerOfTheChunk() {
        factions.members.put(firer, ATTACKERS);
        factions.claim(WORLD, 0, 0, ENEMIES);
        factions.wars.add(ATTACKERS + ":" + ENEMIES);
    }

    @Test
    void aShotMayBreakBlocksInAChunkClaimedByAFactionAtWarWithTheFirers() {
        firerIsAtWarWithTheOwnerOfTheChunk();

        assertTrue(gate.mayBreakBlockAt(firer, WORLD, 5, 5));
    }

    @Test
    void anUnclaimedChunkKeepsItsBlocks() {
        firerIsAtWarWithTheOwnerOfTheChunk();

        assertFalse(gate.mayBreakBlockAt(firer, WORLD, 5, 21));
    }

    @Test
    void aChunkClaimedByAFactionNotAtWarWithTheFirersKeepsItsBlocks() {
        firerIsAtWarWithTheOwnerOfTheChunk();
        factions.claim(WORLD, 1, 0, NEUTRALS);

        assertFalse(gate.mayBreakBlockAt(firer, WORLD, 21, 5));
    }

    @Test
    void theFirersOwnClaimKeepsItsBlocksEvenIfTheFactionsReportASelfWar() {
        firerIsAtWarWithTheOwnerOfTheChunk();
        factions.claim(WORLD, 1, 0, ATTACKERS);
        factions.wars.add(ATTACKERS + ":" + ATTACKERS);

        assertFalse(gate.mayBreakBlockAt(firer, WORLD, 21, 5));
    }

    @Test
    void aFirerWithNoFactionBreaksNothing() {
        firerIsAtWarWithTheOwnerOfTheChunk();
        factions.members.remove(firer);

        assertFalse(gate.mayBreakBlockAt(firer, WORLD, 5, 5));
    }

    @Test
    void aShotWithNoKnownFirerBreaksNothing() {
        firerIsAtWarWithTheOwnerOfTheChunk();
        // Even if the lookup would answer for a null player, the gate must not ask it.
        factions.members.put(null, ATTACKERS);

        assertFalse(gate.mayBreakBlockAt(null, WORLD, 5, 5));
    }

    @Test
    void aClaimInAnotherWorldDoesNotCount() {
        firerIsAtWarWithTheOwnerOfTheChunk();

        assertFalse(gate.mayBreakBlockAt(firer, "world_nether", 5, 5));
    }

    @Test
    void blockPositionsAreTurnedIntoChunksIncludingBelowZero() {
        firerIsAtWarWithTheOwnerOfTheChunk();
        factions.claim(WORLD, -1, -2, ENEMIES);

        assertTrue(gate.mayBreakBlockAt(firer, WORLD, 0, 15));
        assertFalse(gate.mayBreakBlockAt(firer, WORLD, 16, 15), "the next chunk east is unclaimed");
        assertTrue(gate.mayBreakBlockAt(firer, WORLD, -1, -17));
        assertTrue(gate.mayBreakBlockAt(firer, WORLD, -16, -32));
        assertFalse(gate.mayBreakBlockAt(firer, WORLD, -1, -16), "the next chunk south is unclaimed");
    }

    @Test
    void theClosedGateNeverLetsAShotBreakBlocks() {
        WarGate closed = WarGate.closed();

        assertFalse(closed.mayBreakBlockAt(firer, WORLD, 5, 5));
        assertFalse(closed.mayBreakBlockAt(null, WORLD, 5, 5));
    }

    /** The lookup fails only once the gate asks about the war, so every earlier answer was a yes. */
    private WarGate gateWhoseWarLookupThrows(Error error) {
        return new WarGate(new WarGate.Factions() {
            @Override
            public Optional<String> factionOf(UUID player) {
                return Optional.of(ATTACKERS);
            }

            @Override
            public Optional<String> claimOwner(String world, int chunkX, int chunkZ) {
                return Optional.of(ENEMIES);
            }

            @Override
            public boolean atWar(String faction, String otherFaction) {
                throw error;
            }
        }, warnings::add);
    }

    @Test
    void aLookupThatIsNoLongerCompatibleBreaksNothing() {
        WarGate broken = gateWhoseWarLookupThrows(new NoSuchMethodError("getFactionsAtWarWithByFactionId"));

        assertFalse(broken.mayBreakBlockAt(firer, WORLD, 5, 5));
    }

    @Test
    void aLookupThatFailsBreaksNothing() {
        WarGate broken = new WarGate(new WarGate.Factions() {
            @Override
            public Optional<String> factionOf(UUID player) {
                throw new IllegalStateException("services not ready");
            }

            @Override
            public Optional<String> claimOwner(String world, int chunkX, int chunkZ) {
                return Optional.of(ENEMIES);
            }

            @Override
            public boolean atWar(String faction, String otherFaction) {
                return true;
            }
        }, warnings::add);

        assertFalse(broken.mayBreakBlockAt(firer, WORLD, 5, 5));
    }

    @Test
    void aFailingLookupIsReportedOnceNotOncePerBlock() {
        WarGate broken = gateWhoseWarLookupThrows(new NoSuchMethodError("getFactionsAtWarWithByFactionId"));

        broken.mayBreakBlockAt(firer, WORLD, 5, 5);
        broken.mayBreakBlockAt(firer, WORLD, 6, 5);

        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("getFactionsAtWarWithByFactionId"), warnings.get(0));
    }

    @Test
    void anErrorThatIsNotAboutTheLookupIsNotSwallowed() {
        WarGate broken = gateWhoseWarLookupThrows(new OutOfMemoryError("not the gate's to hide"));

        assertThrows(OutOfMemoryError.class, () -> broken.mayBreakBlockAt(firer, WORLD, 5, 5));
    }
}
