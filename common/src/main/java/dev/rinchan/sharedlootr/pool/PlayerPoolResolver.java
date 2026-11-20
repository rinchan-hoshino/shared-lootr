package dev.rinchan.sharedlootr.pool;

import dev.rinchan.sharedlootr.config.SharedLootrConfig;
import dev.rinchan.sharedlootr.config.SharingMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Minecraft player-aware bridge to {@link LootPoolResolver} for server and client resolution.
 */
public final class PlayerPoolResolver {
    private static final Map<UUID, UUID> LAST_KNOWN_SERVER_POOLS = new ConcurrentHashMap<>();
    private static volatile Function<UUID, Player> serverPlayerLookup;

    private PlayerPoolResolver() {
    }

    public static void setServerPlayerLookup(Function<UUID, Player> lookup) {
        serverPlayerLookup = lookup;
    }

    public static UUID resolveServer(Player player) {
        SharedLootrConfig config = SharedLootrConfig.get();
        if (config.sharingMode() == SharingMode.GLOBAL) {
            return LootPoolResolver.GLOBAL_POOL_UUID;
        }
        if (player == null) {
            return LootPoolResolver.GLOBAL_POOL_UUID;
        }
        UUID playerUuid = player.getUUID();
        UUID ftbTeamUuid = FtbTeamsBridge.resolveServerTeam(playerUuid);
        Team scoreboardTeam = player.getTeam();
        String vanillaTeamName = scoreboardTeam != null ? scoreboardTeam.getName() : null;
        UUID resolved = LootPoolResolver.resolvePoolUuid(config, playerUuid, ftbTeamUuid, vanillaTeamName);
        LAST_KNOWN_SERVER_POOLS.put(playerUuid, resolved);
        return resolved;
    }

    public static UUID resolveServerByUuid(UUID playerUuid) {
        SharedLootrConfig config = SharedLootrConfig.get();
        if (config.sharingMode() == SharingMode.GLOBAL) {
            return LootPoolResolver.GLOBAL_POOL_UUID;
        }
        if (playerUuid == null) {
            return LootPoolResolver.GLOBAL_POOL_UUID;
        }
        Function<UUID, Player> lookup = serverPlayerLookup;
        if (lookup != null) {
            Player livePlayer = lookup.apply(playerUuid);
            if (livePlayer != null) {
                return resolveServer(livePlayer);
            }
        }
        UUID ftbTeamUuid = FtbTeamsBridge.resolveServerTeam(playerUuid);
        if (ftbTeamUuid != null) {
            LAST_KNOWN_SERVER_POOLS.put(playerUuid, ftbTeamUuid);
            return ftbTeamUuid;
        }
        UUID cached = LAST_KNOWN_SERVER_POOLS.get(playerUuid);
        if (cached != null) {
            return cached;
        }
        return LootPoolResolver.resolvePoolUuid(config, playerUuid, null, null);
    }

    public static UUID resolveClient(Player player) {
        SharedLootrConfig config = SharedLootrConfig.get();
        if (config.sharingMode() == SharingMode.GLOBAL) {
            return LootPoolResolver.GLOBAL_POOL_UUID;
        }
        if (player == null) {
            return LootPoolResolver.GLOBAL_POOL_UUID;
        }
        UUID playerUuid = player.getUUID();
        UUID ftbTeamUuid = FtbTeamsBridge.resolveClientTeam(playerUuid);
        Team scoreboardTeam = player.getTeam();
        String vanillaTeamName = scoreboardTeam != null ? scoreboardTeam.getName() : null;
        return LootPoolResolver.resolvePoolUuid(config, playerUuid, ftbTeamUuid, vanillaTeamName);
    }

    public static void clearCache() {
        LAST_KNOWN_SERVER_POOLS.clear();
    }
}
