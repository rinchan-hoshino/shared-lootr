package dev.rinchan.sharedlootr;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.fml.common.Mod;

@Mod(SharedLootrNeoForge.MOD_ID)
public final class SharedLootrNeoForge {
    public static final String MOD_ID = "shared_lootr";

    public SharedLootrNeoForge() {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            NeoForge.EVENT_BUS.addListener(this::runSmoke);
        }
    }

    private void runSmoke(ServerStartedEvent event) {
        ChestDataSmokeHarness.run(
                event.getServer(),
                "noobanidus.mods.lootr.data.ChestData",
                "noobanidus.mods.lootr.api.IHasOpeners",
                "noobanidus.mods.lootr.util.ChestUtil"
        );
    }
}
