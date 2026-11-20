package dev.rinchan.sharedlootr.neoforge;

import dev.rinchan.sharedlootr.SharedLootr;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(SharedLootr.MOD_ID)
public final class SharedLootrNeoForge {
    public SharedLootrNeoForge() {
        SharedLootr.init(FMLPaths.CONFIGDIR.get());
    }
}
