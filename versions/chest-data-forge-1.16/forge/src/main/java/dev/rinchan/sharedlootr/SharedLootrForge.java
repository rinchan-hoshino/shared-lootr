package dev.rinchan.sharedlootr;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

@Mod(SharedLootrForge.MOD_ID)
public final class SharedLootrForge {
    public static final String MOD_ID = "shared_lootr";

    public SharedLootrForge() {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SubscribeEvent
    public void runSmoke(FMLServerStartedEvent event) {
        ChestDataSmokeHarness.run(event.getServer(),
                "noobanidus.mods.lootr.data.ChestData",
                "noobanidus.mods.lootr.util.ChestUtil");
    }
}
