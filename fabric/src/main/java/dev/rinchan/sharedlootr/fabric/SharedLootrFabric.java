package dev.rinchan.sharedlootr.fabric;

import dev.rinchan.sharedlootr.SharedLootr;
import dev.rinchan.sharedlootr.pool.PlayerPoolResolver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public final class SharedLootrFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SharedLootr.init(FabricLoader.getInstance().getConfigDir());
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                PlayerPoolResolver.setServerPlayerLookup(uuid -> server.getPlayerList().getPlayer(uuid)));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PlayerPoolResolver.setServerPlayerLookup(null);
            PlayerPoolResolver.clearCache();
        });
    }
}
