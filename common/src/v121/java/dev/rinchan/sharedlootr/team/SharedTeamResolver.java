package dev.rinchan.sharedlootr.team;

import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.common.api.team.ITeamResolver;

import java.util.UUID;

/**
 * Official Lootr {@link ITeamResolver} implementation for Minecraft 1.21.1.
 */
public final class SharedTeamResolver implements ITeamResolver {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SharedLootr.MOD_ID, "shared");

    @Override
    public UUID resolveServerPlayer(Player player) {
        return PlayerPoolResolver.resolveServer(player);
    }

    @Override
    public UUID resolveClientPlayer(Player player) {
        return PlayerPoolResolver.resolveClient(player);
    }

    @Override
    public ResourceLocation resolverId() {
        return ID;
    }

    @Override
    public int priority() {
        return 1000;
    }
}
