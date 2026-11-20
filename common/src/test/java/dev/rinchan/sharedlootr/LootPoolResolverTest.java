package dev.rinchan.sharedlootr;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import dev.rinchan.sharedlootr.pool.LootPoolResolver;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LootPoolResolverTest {
    private static final UUID ALICE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BOB = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FTB_ALPHA = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID FTB_BETA = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void globalModeAlwaysResolvesToLegacyFixedUuid() {
        UUID expected = UUID.fromString("00000000-0000-0000-0000-000000000001");
        assertEquals(expected, LootPoolResolver.GLOBAL_POOL_UUID);
        assertEquals(expected, LootPoolResolver.resolvePoolUuid(SharingMode.GLOBAL, false, ALICE, null, null));
        assertEquals(expected, LootPoolResolver.resolvePoolUuid(SharingMode.GLOBAL, true, BOB, FTB_ALPHA, "red"));
        assertEquals(expected, LootPoolResolver.resolvePoolUuid(null, false, ALICE, FTB_BETA, "blue"));
    }

    @Test
    void teamsModePrefersFtbTeamOverVanillaScoreboardTeam() {
        UUID alicePool = LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, ALICE, FTB_ALPHA, "red");
        UUID bobPool = LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, BOB, FTB_ALPHA, "blue");
        UUID otherTeamPool = LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, BOB, FTB_BETA, "red");

        assertEquals(FTB_ALPHA, alicePool);
        assertEquals(alicePool, bobPool);
        assertEquals(FTB_BETA, otherTeamPool);
        assertNotEquals(alicePool, otherTeamPool);
    }

    @Test
    void teamsModeDerivesDeterministicUuidForVanillaScoreboardTeam() {
        UUID expectedRed = UUID.nameUUIDFromBytes("shared_lootr:vanilla_team:red".getBytes(StandardCharsets.UTF_8));
        UUID alicePool = LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, ALICE, null, "red");
        UUID bobPool = LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, BOB, null, "red");
        UUID bluePool = LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, BOB, null, "blue");

        assertEquals(expectedRed, alicePool);
        assertEquals(alicePool, bobPool);
        assertNotEquals(alicePool, bluePool);
        assertNotEquals(LootPoolResolver.GLOBAL_POOL_UUID, alicePool);
        assertNotEquals(LootPoolResolver.SOLO_POOL_UUID, alicePool);
    }

    @Test
    void teamsModeKeepsSoloPlayersIsolatedByDefault() {
        SharedLootrConfig cfg = new SharedLootrConfig(SharingMode.TEAMS, false);
        UUID alicePool = LootPoolResolver.resolvePoolUuid(cfg, ALICE, null, null);
        UUID bobPool = LootPoolResolver.resolvePoolUuid(cfg, BOB, null, "   ");

        assertEquals(ALICE, alicePool);
        assertEquals(BOB, bobPool);
        assertNotEquals(alicePool, bobPool);
        assertNotEquals(LootPoolResolver.GLOBAL_POOL_UUID, alicePool);
    }

    @Test
    void teamsModeCanShareDedicatedPoolForSoloPlayersWhenEnabled() {
        SharedLootrConfig cfg = new SharedLootrConfig(SharingMode.TEAMS, true);
        UUID alicePool = LootPoolResolver.resolvePoolUuid(cfg, ALICE, null, null);
        UUID bobPool = LootPoolResolver.resolvePoolUuid(cfg, BOB, null, "");

        assertEquals(LootPoolResolver.SOLO_POOL_UUID, alicePool);
        assertEquals(alicePool, bobPool);
        assertNotEquals(LootPoolResolver.GLOBAL_POOL_UUID, alicePool);
    }

    @Test
    void teamsModeNullPlayerUuidFallsBackSafely() {
        assertEquals(
                LootPoolResolver.GLOBAL_POOL_UUID,
                LootPoolResolver.resolvePoolUuid(SharingMode.TEAMS, false, null, null, null));
    }
}
