package dev.rinchan.sharedlootr;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class SharedLootrFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                InventoryStoreSmokeHarness.run(server);
                server.halt(false);
            });
        }
    }
}
