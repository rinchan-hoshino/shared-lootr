package dev.rinchan.sharedlootr;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class SharedLootrFabric implements ModInitializer {
    public static final String MOD_ID = "shared_lootr";

    @Override
    public void onInitialize() {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            ServerLifecycleEvents.SERVER_STARTED.register(server ->
                    ChestDataSmokeHarness.run(
                            server,
                            "net.zestyblaze.lootr.data.ChestData",
                            "net.zestyblaze.lootr.api.IHasOpeners",
                            "net.zestyblaze.lootr.util.ChestUtil"
                    )
            );
        }
    }
}
