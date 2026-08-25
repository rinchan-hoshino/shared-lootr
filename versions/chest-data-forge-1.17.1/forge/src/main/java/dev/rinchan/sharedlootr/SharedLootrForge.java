package dev.rinchan.sharedlootr;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(SharedLootrForge.MOD_ID)
public final class SharedLootrForge {
    public static final String MOD_ID = "shared_lootr";

    public SharedLootrForge() {
        if (Boolean.getBoolean("sharedLootr.smoke")) {
            MinecraftForge.EVENT_BUS.addListener(this::runSmoke);
        }
    }

    private void runSmoke(FMLServerStartedEvent event) {
        ChestDataSmokeHarness.run(
                event.getServer(),
                "noobanidus.mods.lootr.data.NewChestData",
                "noobanidus.mods.lootr.api.IHasOpeners",
                "noobanidus.mods.lootr.util.ChestUtil"
        );
    }
}
