package dev.rinchan.sharedlootr.e2e;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@Mod(SharedLootrE2eMod.MOD_ID)
public final class SharedLootrE2eMod {
    public static final String MOD_ID = "shared_lootr_e2e";

    public SharedLootrE2eMod(IEventBus modEventBus) {
        modEventBus.addListener(SharedLootrE2eMod::onRegisterGameTests);
    }

    private static void onRegisterGameTests(RegisterGameTestsEvent event) {
        event.register(SharedLootrPackagedJarGameTests.class);
    }
}
