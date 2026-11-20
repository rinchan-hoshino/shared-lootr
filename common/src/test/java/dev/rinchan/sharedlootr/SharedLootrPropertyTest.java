package dev.rinchan.sharedlootr;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import dev.rinchan.sharedlootr.config.SimpleToml;
import dev.rinchan.sharedlootr.pool.LootPoolResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Fixed-seed randomized property tests (>= 1,000 trials per invariant) verifying:
 * - Pool UUID resolution determinism, symmetry, transitivity, priority, and isolation across random team topologies.
 * - TOML serialization/parsing round-trip consistency and fuzz resilience.
 */
class SharedLootrPropertyTest {
    private static final long FIXED_SEED = 0x5A17_C0DE_2026L;
    private static final int TOPOLOGY_TRIALS = 2_000;
    private static final int TOML_TRIALS = 1_500;

    private record PlayerState(UUID playerUuid, UUID ftbTeamUuid, String vanillaTeamName) {
    }

    @Test
    void poolResolutionSatisfiesDeterminismSymmetryAndIsolationAcross2000RandomTopologies() {
        Random rng = new Random(FIXED_SEED);

        for (int trial = 0; trial < TOPOLOGY_TRIALS; trial++) {
            SharingMode mode = switch (rng.nextInt(3)) {
                case 0 -> SharingMode.GLOBAL;
                case 1 -> SharingMode.TEAMS;
                default -> null;
            };
            boolean soloPlayersSharePool = rng.nextBoolean();
            SharedLootrConfig config = mode != null ? new SharedLootrConfig(mode, soloPlayersSharePool) : null;

            int ftbTeamCount = 1 + rng.nextInt(5);
            List<UUID> ftbTeams = new ArrayList<>(ftbTeamCount);
            for (int i = 0; i < ftbTeamCount; i++) {
                ftbTeams.add(randomNonReservedUuid(rng));
            }

            int vanillaTeamCount = 1 + rng.nextInt(5);
            List<String> vanillaTeams = new ArrayList<>(vanillaTeamCount);
            for (int i = 0; i < vanillaTeamCount; i++) {
                vanillaTeams.add("team_" + trial + "_" + i);
            }

            int playerCount = 2 + rng.nextInt(6);
            List<PlayerState> players = new ArrayList<>(playerCount);
            for (int i = 0; i < playerCount; i++) {
                UUID playerUuid = randomNonReservedUuid(rng);
                UUID ftbTeam = rng.nextInt(3) == 0 ? ftbTeams.get(rng.nextInt(ftbTeamCount)) : null;
                String vanillaTeam = switch (rng.nextInt(4)) {
                    case 0 -> vanillaTeams.get(rng.nextInt(vanillaTeamCount));
                    case 1 -> "";
                    case 2 -> "   ";
                    default -> null;
                };
                players.add(new PlayerState(playerUuid, ftbTeam, vanillaTeam));
            }

            List<UUID> resolved = new ArrayList<>(playerCount);
            for (PlayerState p : players) {
                UUID first = LootPoolResolver.resolvePoolUuid(
                        mode, soloPlayersSharePool, p.playerUuid(), p.ftbTeamUuid(), p.vanillaTeamName());
                UUID second = LootPoolResolver.resolvePoolUuid(
                        mode, soloPlayersSharePool, p.playerUuid(), p.ftbTeamUuid(), p.vanillaTeamName());

                // Determinism
                assertNotNull(first, "Resolved pool UUID must never be null (trial " + trial + ")");
                assertEquals(first, second, "Resolution must be deterministic (trial " + trial + ")");

                if (config != null) {
                    UUID viaConfig = LootPoolResolver.resolvePoolUuid(
                            config, p.playerUuid(), p.ftbTeamUuid(), p.vanillaTeamName());
                    assertEquals(first, viaConfig, "Config overload must match explicit overload (trial " + trial + ")");
                }

                // Mode-specific invariants
                if (mode == null || mode == SharingMode.GLOBAL) {
                    assertEquals(LootPoolResolver.GLOBAL_POOL_UUID, first,
                            "GLOBAL/null mode must always resolve to GLOBAL_POOL_UUID");
                } else if (p.ftbTeamUuid() != null) {
                    assertEquals(p.ftbTeamUuid(), first,
                            "TEAMS mode must prioritize FTB team UUID over vanilla team and solo settings");
                } else if (p.vanillaTeamName() != null && !p.vanillaTeamName().isBlank()) {
                    assertEquals(LootPoolResolver.vanillaTeamUuid(p.vanillaTeamName()), first,
                            "TEAMS mode must deterministically hash non-blank vanilla team names");
                    assertNotEquals(LootPoolResolver.GLOBAL_POOL_UUID, first);
                    assertNotEquals(LootPoolResolver.SOLO_POOL_UUID, first);
                } else if (soloPlayersSharePool) {
                    assertEquals(LootPoolResolver.SOLO_POOL_UUID, first,
                            "Solo players must share SOLO_POOL_UUID when soloPlayersSharePool=true");
                    assertNotEquals(LootPoolResolver.GLOBAL_POOL_UUID, first);
                } else {
                    assertEquals(p.playerUuid(), first,
                            "Solo players must stay isolated to their own UUID when soloPlayersSharePool=false");
                    assertNotEquals(LootPoolResolver.GLOBAL_POOL_UUID, first);
                    assertNotEquals(LootPoolResolver.SOLO_POOL_UUID, first);
                }

                resolved.add(first);
            }

            // Pairwise symmetry & isolation invariants
            for (int i = 0; i < playerCount; i++) {
                for (int j = i + 1; j < playerCount; j++) {
                    PlayerState a = players.get(i);
                    PlayerState b = players.get(j);
                    UUID poolA = resolved.get(i);
                    UUID poolB = resolved.get(j);

                    boolean expectedSame = expectedSamePool(mode, soloPlayersSharePool, a, b);
                    if (expectedSame) {
                        assertEquals(poolA, poolB, "Expected shared pool in trial " + trial);
                        assertEquals(poolB, poolA, "Symmetry violated in trial " + trial);
                    } else {
                        assertNotEquals(poolA, poolB, "Expected isolated pools in trial " + trial);
                    }
                }
            }
        }
    }

    @Test
    void tomlSerializationAndParsingRoundTripAcross1500RandomInputs(@TempDir Path tempDir) {
        Random rng = new Random(FIXED_SEED ^ 0xCAFE_BABEL);
        SharedLootrConfig.init(tempDir);

        try {
            // 1. Exhaustive + randomized SharedLootrConfig TOML round-trips (including disk persistence)
            for (int trial = 0; trial < TOML_TRIALS; trial++) {
                SharingMode mode = rng.nextBoolean() ? SharingMode.GLOBAL : SharingMode.TEAMS;
                boolean soloShare = rng.nextBoolean();
                SharedLootrConfig original = new SharedLootrConfig(mode, soloShare);

                String serialized = original.toToml();
                SharedLootrConfig parsed = SharedLootrConfig.fromToml(serialized);
                assertEquals(original, parsed, "TOML round-trip mismatch at trial " + trial);
                assertEquals(serialized, parsed.toToml(), "Canonical TOML output must be idempotent");

                // Every 50th trial, verify full disk write + reload round-trip
                if (trial % 50 == 0) {
                    SharedLootrConfig.updateAndSave(original);
                    SharedLootrConfig reloaded = SharedLootrConfig.reload();
                    assertEquals(original, reloaded, "Disk round-trip mismatch at trial " + trial);
                }

                // 2. SimpleToml string quoting/unquoting round-trip with special characters
                String randomValue = randomTomlValue(rng);
                String lineEnding = rng.nextBoolean() ? "\n" : "\r\n";
                String comment = rng.nextBoolean() ? " # trailing comment " + trial : "";
                String rawDoc = "# header" + lineEnding
                        + "[general]" + lineEnding
                        + "  customKey = " + SimpleToml.quote(randomValue) + comment + lineEnding;
                Map<String, String> map = SimpleToml.parse(rawDoc);
                assertEquals(randomValue, map.get("customKey"),
                        "SimpleToml quote/parse round-trip failed at trial " + trial);

                // 3. Fuzz malformed / mixed-case TOML lines: never throw, always produce valid config
                String fuzzed = generateFuzzedToml(rng);
                SharedLootrConfig safe = SharedLootrConfig.fromToml(fuzzed);
                assertNotNull(safe);
                assertNotNull(safe.sharingMode());
            }
        } finally {
            SharedLootrConfig.updateAndSave(SharedLootrConfig.defaults());
        }
    }

    private static boolean expectedSamePool(
            SharingMode mode,
            boolean soloPlayersSharePool,
            PlayerState a,
            PlayerState b) {
        if (mode == null || mode == SharingMode.GLOBAL) {
            return true;
        }
        if (a.ftbTeamUuid() != null || b.ftbTeamUuid() != null) {
            return a.ftbTeamUuid() != null && a.ftbTeamUuid().equals(b.ftbTeamUuid());
        }
        boolean aVanilla = a.vanillaTeamName() != null && !a.vanillaTeamName().isBlank();
        boolean bVanilla = b.vanillaTeamName() != null && !b.vanillaTeamName().isBlank();
        if (aVanilla || bVanilla) {
            return aVanilla && bVanilla && a.vanillaTeamName().equals(b.vanillaTeamName());
        }
        return soloPlayersSharePool || a.playerUuid().equals(b.playerUuid());
    }

    private static UUID randomNonReservedUuid(Random rng) {
        UUID candidate;
        do {
            candidate = new UUID(rng.nextLong(), rng.nextLong());
        } while (candidate.equals(LootPoolResolver.GLOBAL_POOL_UUID)
                || candidate.equals(LootPoolResolver.SOLO_POOL_UUID));
        return candidate;
    }

    private static String randomTomlValue(Random rng) {
        String alphabet = "abcXYZ0123456789 _-#\"\\/:=中文日本語";
        int len = 1 + rng.nextInt(32);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private static String generateFuzzedToml(Random rng) {
        String[] modeTokens = {"\"GLOBAL\"", "\"TEAMS\"", "\"teams\"", "\"TEAM\"", "\"unknown\"", "''", "global", ""};
        String[] boolTokens = {"true", "FALSE", "True", "yes", "0", "\"true\"", ""};
        return "# fuzz\n"
                + "[section_" + rng.nextInt(10) + "]\n"
                + "sharingMode = " + modeTokens[rng.nextInt(modeTokens.length)] + "\n"
                + "soloPlayersSharePool = " + boolTokens[rng.nextInt(boolTokens.length)] + "\n"
                + "garbage_without_equals\n"
                + "=missing_key\n";
    }
}
