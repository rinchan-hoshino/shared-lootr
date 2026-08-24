package dev.rinchan.sharedlootr;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod(SharedLootrNeoForge.MOD_ID)
public final class SharedLootrNeoForge {
    public static final String MOD_ID = "shared_lootr";

    public SharedLootrNeoForge(IEventBus ignoredModBus) {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        SavedDataSmokeHarness.run();
        event.getServer().halt(false);
    }
}
