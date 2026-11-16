package dev.rinchan.sharedlootr.team;

import dev.rinchan.sharedlootr.rinlib.state.SharedOwnerState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import noobanidus.mods.lootr.common.api.interfaces.processor.ITeamResolver;

import java.util.UUID;

/**
 * Resolves every player to the single shared owner UUID via Lootr 26.2's team API.
 */
public final class SharedTeamResolver implements ITeamResolver {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("shared_lootr", "shared");

    @Override
    public UUID resolveServerPlayer(Player player) {
        return SharedOwnerState.OWNER;
    }

    @Override
    public UUID resolveClientPlayer(Player player) {
        return SharedOwnerState.OWNER;
    }

    @Override
    public Identifier resolverId() {
        return ID;
    }

    @Override
    public int priority() {
        return 1000;
    }
}
