package dev.rinchan.sharedlootr;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("shared_lootr")
public final class SharedLootrNeoForge {
    public SharedLootrNeoForge() {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        }
    }

    private void onServerStarted(ServerStartedEvent event) {
        InventoryStoreSmokeHarness.run(event.getServer());
        event.getServer().halt(false);
    }
}
