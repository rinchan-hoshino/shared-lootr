package dev.rinchan.sharedlootr.pool;

import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Pure, deterministic loot pool UUID resolver supporting GLOBAL and TEAMS sharing modes.
 */
public final class LootPoolResolver {
    public static final UUID GLOBAL_POOL_UUID = SharedLootr.GLOBAL_POOL_UUID;
    public static final String VANILLA_TEAM_PREFIX = "shared_lootr:vanilla_team:";
    public static final UUID SOLO_POOL_UUID = UUID.nameUUIDFromBytes(
            "shared_lootr:solo_pool".getBytes(StandardCharsets.UTF_8));

    private LootPoolResolver() {
    }

    public static UUID vanillaTeamUuid(String teamName) {
        return UUID.nameUUIDFromBytes((VANILLA_TEAM_PREFIX + teamName).getBytes(StandardCharsets.UTF_8));
    }

    public static UUID resolvePoolUuid(
            SharedLootrConfig config,
            UUID playerUuid,
            UUID ftbTeamUuid,
            String vanillaTeamName) {
        SharedLootrConfig effective = config != null ? config : SharedLootrConfig.defaults();
        return resolvePoolUuid(
                effective.sharingMode(),
                effective.soloPlayersSharePool(),
                playerUuid,
                ftbTeamUuid,
                vanillaTeamName);
    }

    public static UUID resolvePoolUuid(
            SharingMode mode,
            boolean soloPlayersSharePool,
            UUID playerUuid,
            UUID ftbTeamUuid,
            String vanillaTeamName) {
        if (mode == null || mode == SharingMode.GLOBAL) {
            return GLOBAL_POOL_UUID;
        }
        if (ftbTeamUuid != null) {
            return ftbTeamUuid;
        }
        if (vanillaTeamName != null && !vanillaTeamName.isBlank()) {
            return vanillaTeamUuid(vanillaTeamName);
        }
        if (soloPlayersSharePool) {
            return SOLO_POOL_UUID;
        }
        return playerUuid != null ? playerUuid : GLOBAL_POOL_UUID;
    }
}
